package ink.aos.commons.captcha.api;

import ink.aos.commons.captcha.exception.AosCaptchaException;
import ink.aos.commons.captcha.util.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

public class BlockPuzzleCaptcha {

    private static final Logger log = LoggerFactory.getLogger(BlockPuzzleCaptcha.class);

    private static final String IMAGE_TYPE_PNG = "png";

    private static int HAN_ZI_SIZE = 25;

    private static String waterMark = "Aos";

    private static String waterMarkFont = "宋体";

    private static String slipOffset = "5";

    private static int captchaInterferenceOptions = 0;

    private final CaptchaStorage captchaStorage;

    private static List<String> original = new ArrayList<>();  //滑块底图
    private static List<String> slidingBlock = new ArrayList<>();  //滑块

    public BlockPuzzleCaptcha(CaptchaStorage captchaStorage) {
        this.captchaStorage = captchaStorage;
    }

    public Captcha get() throws AosCaptchaException {

        //原生图片
        BufferedImage originalImage = getOriginal();
        if (null == originalImage) {
            throw new AosCaptchaException("滑动底图未初始化成功，请检查路径");
        }
        //设置水印
        Graphics backgroundGraphics = originalImage.getGraphics();
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        Font watermark = new Font(waterMarkFont, Font.BOLD, HAN_ZI_SIZE / 2);
        backgroundGraphics.setFont(watermark);
        backgroundGraphics.setColor(Color.white);
        backgroundGraphics.drawString(waterMark, width - getEnOrChLength(waterMark), height - (HAN_ZI_SIZE / 2) + 7);

        //抠图图片
        String jigsawImageBase64 = getslidingBlock();
        BufferedImage jigsawImage = getBase64StrToImage(jigsawImageBase64);
        if (null == jigsawImage) {
            log.error("滑动底图未初始化成功，请检查路径");
            throw new AosCaptchaException("底图未初始化成功，请检查路径");
        }
        Captcha captcha = pictureTemplatesCut(originalImage, jigsawImage, jigsawImageBase64);
        if (captcha == null
                || StringUtils.isBlank(captcha.getJigsawImageBase64())
                || StringUtils.isBlank(captcha.getOriginalImageBase64())) {
            throw new AosCaptchaException("获取验证码失败,请联系管理员");
        }
        return captcha;
    }

    public void check(CaptchaCheck captchaCheck) throws AosCaptchaException {
        ink.aos.commons.captcha.api.Point point = captchaStorage.getFirstToken(captchaCheck.getToken());

        captchaStorage.removeFirstToken(captchaCheck.getToken());

        ink.aos.commons.captcha.api.Point point1 = captchaCheck.getPoint();

        if (point.x - Integer.parseInt(slipOffset) > point1.x
                || point1.x > point.x + Integer.parseInt(slipOffset)
                || point.y != point1.y) {
            throw new AosCaptchaException("验证失败");
        }
        captchaStorage.saveSecondToken(captchaCheck.getToken());
    }

    public void verification(String token) throws AosCaptchaException {
        boolean flag = captchaStorage.hasSecondToken(token);

        if (!flag) {
            throw new AosCaptchaException("验证码已失效，请重新获取");
        }
        //二次校验取值后，即刻失效
        captchaStorage.removeSecondToken(token);
    }


    /**
     * 根据模板切图
     *
     * @throws Exception
     */
    public Captcha pictureTemplatesCut(BufferedImage originalImage, BufferedImage jigsawImage, String jigsawImageBase64) {
        try {

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            int jigsawWidth = jigsawImage.getWidth();
            int jigsawHeight = jigsawImage.getHeight();

            //随机生成拼图坐标
            ink.aos.commons.captcha.api.Point point = generateJigsawPoint(originalWidth, originalHeight, jigsawWidth, jigsawHeight);
            int x = point.getX();
            int y = point.getY();

            //生成新的拼图图像
            BufferedImage newJigsawImage = new BufferedImage(jigsawWidth, jigsawHeight, jigsawImage.getType());
            Graphics2D graphics = newJigsawImage.createGraphics();

            int bold = 5;
            //如果需要生成RGB格式，需要做如下配置,Transparency 设置透明
            newJigsawImage = graphics.getDeviceConfiguration().createCompatibleImage(jigsawWidth, jigsawHeight, Transparency.TRANSLUCENT);
            // 新建的图像根据模板颜色赋值,源图生成遮罩
            cutByTemplate(originalImage, jigsawImage, newJigsawImage, x, 0);
            if (captchaInterferenceOptions > 0) {
                int position = 0;
                if (originalWidth - x - 5 > jigsawWidth * 2) {
                    //在原扣图右边插入干扰图
                    position = RandomUtils.getRandomInt(x + jigsawWidth + 5, originalWidth - jigsawWidth);
                } else {
                    //在原扣图左边插入干扰图
                    position = RandomUtils.getRandomInt(100, x - jigsawWidth - 5);
                }
                while (true) {
                    String s = getslidingBlock();
                    if (!jigsawImageBase64.equals(s)) {
                        interferenceByTemplate(originalImage, Objects.requireNonNull(getBase64StrToImage(s)), position, 0);
                        break;
                    }
                }
            }
            if (captchaInterferenceOptions > 1) {
                while (true) {
                    String s = getslidingBlock();
                    if (!jigsawImageBase64.equals(s)) {
                        Integer randomInt = RandomUtils.getRandomInt(jigsawWidth, 100 - jigsawWidth);
                        interferenceByTemplate(originalImage, Objects.requireNonNull(getBase64StrToImage(s)),
                                randomInt, 0);
                        break;
                    }
                }
            }


            // 设置“抗锯齿”的属性
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setStroke(new BasicStroke(bold, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            graphics.drawImage(newJigsawImage, 0, 0, null);
            graphics.dispose();

            ByteArrayOutputStream os = new ByteArrayOutputStream();//新建流。
            ImageIO.write(newJigsawImage, IMAGE_TYPE_PNG, os);//利用ImageIO类提供的write方法，将bi以png图片的数据模式写入流。
            byte[] jigsawImages = os.toByteArray();

            ByteArrayOutputStream oriImagesOs = new ByteArrayOutputStream();//新建流。
            ImageIO.write(originalImage, IMAGE_TYPE_PNG, oriImagesOs);//利用ImageIO类提供的write方法，将bi以jpg图片的数据模式写入流。
            byte[] oriCopyImages = oriImagesOs.toByteArray();
            Base64.Encoder encoder = Base64.getEncoder();
            Captcha dataVO = new Captcha();
            dataVO.setOriginalImageBase64(encoder.encodeToString(oriCopyImages).replaceAll("\r|\n", ""));
            dataVO.setJigsawImageBase64(encoder.encodeToString(jigsawImages).replaceAll("\r|\n", ""));
            dataVO.setToken(RandomUtils.getUUID());

            captchaStorage.saveFirstToken(dataVO.getToken(), point);
            return dataVO;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
//
//

    /**
     * 随机生成拼图坐标
     *
     * @param originalWidth
     * @param originalHeight
     * @param jigsawWidth
     * @param jigsawHeight
     * @return
     */
    private static ink.aos.commons.captcha.api.Point generateJigsawPoint(int originalWidth, int originalHeight, int jigsawWidth, int jigsawHeight) {
        Random random = new Random();
        int widthDifference = originalWidth - jigsawWidth;
        int heightDifference = originalHeight - jigsawHeight;
        int x, y = 0;
        if (widthDifference <= 0) {
            x = 5;
        } else {
            x = random.nextInt(originalWidth - jigsawWidth - 100) + 100;
        }
        if (heightDifference <= 0) {
            y = 5;
        } else {
            y = random.nextInt(originalHeight - jigsawHeight) + 5;
        }
        return new Point(x, y);
    }

    /**
     * @param oriImage      原图
     * @param templateImage 模板图
     * @param newImage      新抠出的小图
     * @param x             随机扣取坐标X
     * @param y             随机扣取坐标y
     * @throws Exception
     */
    private static void cutByTemplate(BufferedImage oriImage, BufferedImage templateImage, BufferedImage newImage, int x, int y) {
        //临时数组遍历用于高斯模糊存周边像素值
        int[][] martrix = new int[3][3];
        int[] values = new int[9];

        int xLength = templateImage.getWidth();
        int yLength = templateImage.getHeight();
        // 模板图像宽度
        for (int i = 0; i < xLength; i++) {
            // 模板图片高度
            for (int j = 0; j < yLength; j++) {
                // 如果模板图像当前像素点不是透明色 copy源文件信息到目标图片中
                int rgb = templateImage.getRGB(i, j);
                if (rgb < 0) {
                    newImage.setRGB(i, j, oriImage.getRGB(x + i, y + j));

                    //抠图区域高斯模糊
                    readPixel(oriImage, x + i, y + j, values);
                    fillMatrix(martrix, values);
                    oriImage.setRGB(x + i, y + j, avgMatrix(martrix));
                }

                //防止数组越界判断
                if (i == (xLength - 1) || j == (yLength - 1)) {
                    continue;
                }
                int rightRgb = templateImage.getRGB(i + 1, j);
                int downRgb = templateImage.getRGB(i, j + 1);
                //描边处理，,取带像素和无像素的界点，判断该点是不是临界轮廓点,如果是设置该坐标像素是白色
                if ((rgb >= 0 && rightRgb < 0) || (rgb < 0 && rightRgb >= 0) || (rgb >= 0 && downRgb < 0) || (rgb < 0 && downRgb >= 0)) {
                    newImage.setRGB(i, j, Color.white.getRGB());
                    oriImage.setRGB(x + i, y + j, Color.white.getRGB());
                }
            }
        }

    }

    /**
     * 干扰抠图处理
     *
     * @param oriImage      原图
     * @param templateImage 模板图
     * @param x             随机扣取坐标X
     * @param y             随机扣取坐标y
     * @throws Exception
     */
    private static void interferenceByTemplate(BufferedImage oriImage, BufferedImage templateImage, int x, int y) {
        //临时数组遍历用于高斯模糊存周边像素值
        int[][] martrix = new int[3][3];
        int[] values = new int[9];

        int xLength = templateImage.getWidth();
        int yLength = templateImage.getHeight();
        // 模板图像宽度
        for (int i = 0; i < xLength; i++) {
            // 模板图片高度
            for (int j = 0; j < yLength; j++) {
                // 如果模板图像当前像素点不是透明色 copy源文件信息到目标图片中
                int rgb = templateImage.getRGB(i, j);
                if (rgb < 0) {
                    //抠图区域高斯模糊
                    readPixel(oriImage, x + i, y + j, values);
                    fillMatrix(martrix, values);
                    oriImage.setRGB(x + i, y + j, avgMatrix(martrix));
                }
                //防止数组越界判断
                if (i == (xLength - 1) || j == (yLength - 1)) {
                    continue;
                }
                int rightRgb = templateImage.getRGB(i + 1, j);
                int downRgb = templateImage.getRGB(i, j + 1);
                //描边处理，,取带像素和无像素的界点，判断该点是不是临界轮廓点,如果是设置该坐标像素是白色
                if ((rgb >= 0 && rightRgb < 0) || (rgb < 0 && rightRgb >= 0) || (rgb >= 0 && downRgb < 0) || (rgb < 0 && downRgb >= 0)) {
                    oriImage.setRGB(x + i, y + j, Color.white.getRGB());
                }
            }
        }

    }

    private static void readPixel(BufferedImage img, int x, int y, int[] pixels) {
        int xStart = x - 1;
        int yStart = y - 1;
        int current = 0;
        for (int i = xStart; i < 3 + xStart; i++) {
            for (int j = yStart; j < 3 + yStart; j++) {
                int tx = i;
                if (tx < 0) {
                    tx = -tx;

                } else if (tx >= img.getWidth()) {
                    tx = x;
                }
                int ty = j;
                if (ty < 0) {
                    ty = -ty;
                } else if (ty >= img.getHeight()) {
                    ty = y;
                }
                pixels[current++] = img.getRGB(tx, ty);

            }
        }
    }

    //
    private static void fillMatrix(int[][] matrix, int[] values) {
        int filled = 0;
        for (int i = 0; i < matrix.length; i++) {
            int[] x = matrix[i];
            for (int j = 0; j < x.length; j++) {
                x[j] = values[filled++];
            }
        }
    }

    private static int avgMatrix(int[][] matrix) {
        int r = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < matrix.length; i++) {
            int[] x = matrix[i];
            for (int j = 0; j < x.length; j++) {
                if (j == 1) {
                    continue;
                }
                Color c = new Color(x[j]);
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();
            }
        }
        return new Color(r / 8, g / 8, b / 8).getRGB();
    }

    protected static int getEnOrChLength(String s) {
        int enCount = 0;
        int chCount = 0;
        for (int i = 0; i < s.length(); i++) {
            int length = String.valueOf(s.charAt(i)).getBytes(StandardCharsets.UTF_8).length;
            if (length > 1) {
                chCount++;
            } else {
                enCount++;
            }
        }
        int chOffset = (HAN_ZI_SIZE / 2) * chCount + 5;
        int enOffset = enCount * 8;
        return chOffset + enOffset;
    }

    public static BufferedImage getOriginal() {
        Integer randomInt = RandomUtils.getRandomInt(0, original.size());
        String s = original.get(randomInt);
        return getBase64StrToImage(s);
    }

    public static String getslidingBlock() {
        Integer randomInt = RandomUtils.getRandomInt(0, slidingBlock.size());
        String s = slidingBlock.get(randomInt);
        return s;
    }

    /**
     * base64 字符串转图片
     *
     * @param base64String
     * @return
     */
    public static BufferedImage getBase64StrToImage(String base64String) {
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] bytes = decoder.decode(base64String);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


//    private static Map<String, String> getResourcesImagesFile(String path) throws IOException {
//        //默认提供六张底图
//        Map<String, String> imgMap = new HashMap<>();
//        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
//        Resource[] resources = resourcePatternResolver.getResources(path);
//        for (Resource resource : resources) {
//            String string = Base64Utils.encodeToString(FileCopyUtils.copyToByteArray(resource.getInputStream()));
//            String filename = String.valueOf(resource.getFilename()).concat(".png");
//            imgMap.put(filename, string);
//        }
//        return imgMap;
//    }

    public void addOriginalBase64(String base64) {
        original.add(base64);
    }

    public void addSlidingBlockBase64(String base64) {
        slidingBlock.add(base64);
    }

}
