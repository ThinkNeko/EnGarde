package engarde.gui;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author ktajima
 */
public class Constants {
    //Jarファイル利用時に使うURL
    public static boolean url_enable = false;
    public static String BackGroundImage_url;
    public static String PlayerPownWhite_url;
    public static String PlayerPownBlack_url;
    public static String PointMaker_url;
    //外部ファイルを使う用
    public static boolean file_enable = false;
    public static File dataDir;
    public static File BackGroundImage;
    public static File PlayerPownWhite;
    public static File PlayerPownBlack;
    public static File PointMaker;

    public static Path getApplicationPath(Class<?> cls) throws URISyntaxException {
        ProtectionDomain pd = cls.getProtectionDomain();
        CodeSource cs = pd.getCodeSource();
        URL location = cs.getLocation();
        URI uri = location.toURI();
        Path path = Paths.get(uri);
        return path;
    }
    
    //    フォルダを指定してセットする場合
    public static void setFiles(File datadir){
        dataDir = datadir;
        BackGroundImage = new File(dataDir.getPath() + File.separator + "background.png");
        PlayerPownWhite = new File(dataDir.getPath() + File.separator + "pownw.png");
        PlayerPownBlack = new File(dataDir.getPath() + File.separator + "pownb.png");
        PointMaker = new File(dataDir.getPath() + File.separator + "point.png");
        file_enable = true;
    }
    
    //Jarの内部ファイルをセットする場合
    public static void setFiles(){
        String jPath = "img/";
        BackGroundImage_url = jPath + "background.png";
        PlayerPownWhite_url  = jPath + "pownw.png";
        PlayerPownBlack_url  = jPath + "pownb.png";
        PointMaker_url  = jPath + "point.png";
        url_enable = true;
    }
    
}
