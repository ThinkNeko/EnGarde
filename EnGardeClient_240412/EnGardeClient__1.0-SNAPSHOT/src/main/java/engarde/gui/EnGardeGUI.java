/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package engarde.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author ktaji
 */
public class EnGardeGUI extends javax.swing.JFrame {
    public static BufferedImage background;
    public static BufferedImage[] playerPowns;
    public static BufferedImage scoreMarker;
    
    private static ArrayList<Point> pownDrawPoints;
    private static ArrayList<Point> marker0DrawPoints;
    private static ArrayList<Point> marker1DrawPoints;
    private static ArrayList<Point> player0CardPoints;
    private static ArrayList<Rectangle> player0CardFrame;
    private static ArrayList<Point> player1CardPoints;
    private static ArrayList<Rectangle> player1CardFrame;
    
    private static Rectangle forwardButton;
    private static Rectangle backwardButton;
    private static Rectangle attackButton;
    
    private static Point cardDeckPoint;
    private static Point TextPoint;
    private static Point MessagePoint;
            
    private int currentPlayer = 0;
    private int player0_posion = 1;
    private int player1_posion = 23;
    private int player0_score = 0;
    private int player1_score = 0;
    private int deck_count = 15;
    private int[] player0Cards = {5,3,4,4,5};
    private int[] player1Cards = {2,2,3,3,-1};
    private boolean clientMode = true;
    
    private ArrayList<ActionListener> eventListeners = new ArrayList<>();
    public void addEventListener(ActionListener eventListener){
        this.eventListeners.add(eventListener);
    }
    public void sendEvent(ActionEvent event){
        for(ActionListener e:eventListeners){
            e.actionPerformed(event);
        }
    }
    
    public static void readStatics(){
        try {
            if(Constants.file_enable){
                background = ImageIO.read(Constants.BackGroundImage);
                scoreMarker = ImageIO.read(Constants.PointMaker);
                playerPowns = new BufferedImage[2];
                playerPowns[0] = ImageIO.read(Constants.PlayerPownBlack);
                playerPowns[1] = ImageIO.read(Constants.PlayerPownWhite);
            } else {
                background = ImageIO.read(Constants.class.getClassLoader().getResourceAsStream(Constants.BackGroundImage_url));
                scoreMarker = ImageIO.read(Constants.class.getClassLoader().getResourceAsStream(Constants.PointMaker_url));
                playerPowns = new BufferedImage[4];
                playerPowns[0] = ImageIO.read(Constants.class.getClassLoader().getResourceAsStream(Constants.PlayerPownBlack_url));
                playerPowns[1] = ImageIO.read(Constants.class.getClassLoader().getResourceAsStream(Constants.PlayerPownWhite_url));
            }
        } catch (IOException ex) {
            Logger.getLogger(EnGardeGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        pownDrawPoints = new ArrayList<Point>(23);
        for(int i=0;i<23;i++){
            pownDrawPoints.add(new Point(36+(int)(134.1*i),1016-189));
        }
//        pownDrawPoints.add(new Point(36,1016-189));
//        pownDrawPoints.add(new Point(2988,1016-189));

        marker0DrawPoints = new ArrayList<>(5);
        marker1DrawPoints = new ArrayList<>(5);
        marker0DrawPoints.add(new Point(-10,384));
        marker0DrawPoints.add(new Point(100,384));
        marker0DrawPoints.add(new Point(218,384));
        marker0DrawPoints.add(new Point(325,384));
        marker0DrawPoints.add(new Point(436,384));
        marker0DrawPoints.add(new Point(554,384));
        
        marker1DrawPoints.add(new Point(3100,415));
        marker1DrawPoints.add(new Point(3014,415));
        marker1DrawPoints.add(new Point(2898,415));
        marker1DrawPoints.add(new Point(2788,415));
        marker1DrawPoints.add(new Point(2672,415));
        marker1DrawPoints.add(new Point(2563,415));
        
        player0CardPoints = new ArrayList<>(5);
        player1CardPoints = new ArrayList<>(5);
        player0CardPoints.add(new Point(90,210));
        player0CardPoints.add(new Point(260,210));
        player0CardPoints.add(new Point(440,210));
        player0CardPoints.add(new Point(610,210));
        player0CardPoints.add(new Point(790,210));

        player1CardPoints.add(new Point(2220,210));
        player1CardPoints.add(new Point(2400,210));
        player1CardPoints.add(new Point(2580,210));
        player1CardPoints.add(new Point(2755,210));
        player1CardPoints.add(new Point(2935,210));
        
        player0CardFrame = new ArrayList<>(5);
        player1CardFrame = new ArrayList<>(5);
        player0CardFrame.add(new Rectangle(45,45,165,230));
        player0CardFrame.add(new Rectangle(220,45,165,230));
        player0CardFrame.add(new Rectangle(400,45,165,230));
        player0CardFrame.add(new Rectangle(577,45,165,230));
        player0CardFrame.add(new Rectangle(759,45,165,230));
        
        player1CardFrame.add(new Rectangle(2182,47,165,230));
        player1CardFrame.add(new Rectangle(2358,47,165,230));
        player1CardFrame.add(new Rectangle(2533,47,165,230));
        player1CardFrame.add(new Rectangle(2715,47,165,230));
        player1CardFrame.add(new Rectangle(2893,47,165,230));
        
        cardDeckPoint = new Point(1550,150);
        TextPoint = new Point(973,600);
        
        forwardButton = new Rectangle(1250,350,230,100);
        backwardButton = new Rectangle(1500,350,230,100);
        attackButton = new Rectangle(1750,350,230,100);
        MessagePoint = new Point(973,700);
        
    }
    
    
    /**
     * Creates new form EnGardeGUI
     */
    public EnGardeGUI() {
        readStatics();
        initComponents();
        this.getContentPane().addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {
                EnGardeGUI.this.frameClicked(e.getPoint());
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    //カード関係
    private ArrayList<Rectangle> RectangleList = new ArrayList<>();
    private HashMap<Integer,String> RectangleMapI2S = new HashMap<>();
    private HashMap<String,Integer> RectangleMapS2I = new HashMap<>();
    private ArrayList<String> selectedRectangle = new ArrayList<>();
    //クライアントのボタン関係
    private String[] Button_Name_list = {"Forward","Backward","Attack"};
    private ArrayList<Rectangle> ClientButton_RectangleList = new ArrayList<>();
    private HashMap<Integer,String> ClientButton_RectangleMapI2S = new HashMap<>();
    private HashMap<String,Integer> ClientButton_RectangleMapS2I = new HashMap<>();
    private String uiMessage = "";
    
    public void drawImages(){
        this.RectangleList.clear();
        this.RectangleMapI2S.clear();
        this.RectangleMapS2I.clear();
        
        Graphics g = this.getContentPane().getGraphics();
        //baseimage = W3131*H1197
        int width = this.getWidth() - 10;
        int height = this.getHeight();
        double zoom;
        double zoom_h = height / 1197.0;
        double zoom_w = width / 3131.0;
        //サイズ比を確定：小さいほうに合わせる
        if(zoom_h > zoom_w){
            zoom = zoom_w;
        } else {
            zoom = zoom_h;
        }
        int imgsize_h = (int)(1197*zoom);
        int imgsize_w = (int)(3131*zoom);
        BufferedImage scaledImg = new BufferedImage(imgsize_w, imgsize_h, background.getType());
        scaledImg.createGraphics().drawImage(
            background.getScaledInstance(imgsize_w, imgsize_h, Image.SCALE_AREA_AVERAGING),
                                    0, 0, imgsize_w, imgsize_h, null);
        g.drawImage(scaledImg, 0, 0, this);

        //ポーンの配置
        imgsize_h = (int)(189*zoom);
        imgsize_w = (int)(120*zoom);
        BufferedImage scaledPown_black = new BufferedImage(imgsize_w, imgsize_h, playerPowns[0].getType());
        scaledPown_black.createGraphics().drawImage(
            playerPowns[0].getScaledInstance(imgsize_w, imgsize_h, Image.SCALE_AREA_AVERAGING),
                                    0, 0, imgsize_w, imgsize_h, null);
        double x = pownDrawPoints.get(player0_posion-1).x*zoom;
        double y =  pownDrawPoints.get(player0_posion-1).y*zoom;
        g.drawImage(scaledPown_black, (int)x, (int)y, this);
        
        imgsize_h = (int)(189*zoom);
        imgsize_w = (int)(120*zoom);
        BufferedImage scaledPown_white = new BufferedImage(imgsize_w, imgsize_h, playerPowns[1].getType());
        scaledPown_white.createGraphics().drawImage(
            playerPowns[1].getScaledInstance(imgsize_w, imgsize_h, Image.SCALE_AREA_AVERAGING),
                                    0, 0, imgsize_w, imgsize_h, null);
        x = pownDrawPoints.get(player1_posion-1).x*zoom;
        y = pownDrawPoints.get(player1_posion-1).y*zoom;
        g.drawImage(scaledPown_white, (int)x, (int)y, this);
        
        //スコアマーカー
        imgsize_h = (int)(97*zoom);
        imgsize_w = (int)(108*zoom);
        BufferedImage scaledScoreMarker = new BufferedImage(imgsize_w, imgsize_h, scoreMarker.getType());
        scaledScoreMarker.createGraphics().drawImage(
            scoreMarker.getScaledInstance(imgsize_w, imgsize_h, Image.SCALE_AREA_AVERAGING),
                                    0, 0, imgsize_w, imgsize_h, null);
        x = (marker0DrawPoints.get(player0_score).x - 40)*zoom; //描画点は中心座標になっている
        y = (marker0DrawPoints.get(player0_score).y - 40)*zoom;
        g.drawImage(scaledScoreMarker, (int)x, (int)y, this);
        x = (marker1DrawPoints.get(player1_score).x -40)*zoom;
        y = (marker1DrawPoints.get(player1_score).y -40)*zoom;
        g.drawImage(scaledScoreMarker, (int)x, (int)y, this);
    
        //デッキのカードとプレイヤーのカード
        Font base = g.getFont();
        
        int fontsize = (int)(150*zoom);
        Font fg = new Font("SansSerif" , Font.PLAIN , fontsize);
        g.setFont(fg);
        x = (cardDeckPoint.x)*zoom;
        y = (cardDeckPoint.y)*zoom;
        g.drawString(Integer.toString(deck_count), (int)x, (int)y);
        
        for(int i=0;i<5;i++){
            int num = player0Cards[i];
            x = (player0CardPoints.get(i).x)*zoom;
            y = (player0CardPoints.get(i).y)*zoom;
            if(num != -1){
                //カードの場所を記録
                Rectangle base_rect = player0CardFrame.get(i);
                Rectangle rect = new Rectangle((int)(base_rect.x*zoom),(int)(base_rect.y*zoom),(int)(base_rect.width*zoom),(int)(base_rect.height*zoom));
                int id = this.RectangleList.size();
                String idName = "Player0_Card" + (i+1);
                this.RectangleMapI2S.put(id, idName);
                this.RectangleMapS2I.put(idName, id);
                this.RectangleList.add(rect);
                //選ばれていたら背景を塗る
                if(this.selectedRectangle.contains(idName)){
                    g.setColor(Color.RED);
                    g.fillRect(rect.x, rect.y, rect.width, rect.height);
                    g.setColor(Color.BLACK);
                }
                //文字を書く
                g.drawString(Integer.toString(num), (int)x, (int)y);
                
            } else {
                g.drawString("-", (int)x, (int)y);
            }
            num = player1Cards[i];
            x = (player1CardPoints.get(i).x)*zoom;
            y = (player1CardPoints.get(i).y)*zoom;
            if(num != -1){
                //カードの場所を記録
                Rectangle base_rect = player1CardFrame.get(i);
                Rectangle rect = new Rectangle((int)(base_rect.x*zoom),(int)(base_rect.y*zoom),(int)(base_rect.width*zoom),(int)(base_rect.height*zoom));
                int id = this.RectangleList.size();
                String idName = "Player1_Card" + (i+1);
                this.RectangleMapI2S.put(id, idName);
                this.RectangleMapS2I.put(idName, id);
                this.RectangleList.add(rect);
                //選ばれていたら背景を塗る
                if(this.selectedRectangle.contains(idName)){
                    g.setColor(Color.RED);
                    g.fillRect(rect.x, rect.y, rect.width, rect.height);
                    g.setColor(Color.BLACK);
                }
                //文字を書く
                g.drawString(Integer.toString(num), (int)x, (int)y);
            } else {
                g.drawString("-", (int)x, (int)y);
            }
        }
        g.setFont(base);
        //
        
        //操作UIの描画
        if(clientMode){
            Rectangle[] rec_list = {forwardButton,backwardButton,attackButton};
            ClientButton_RectangleList = new ArrayList<>();
            ClientButton_RectangleMapI2S = new HashMap<>();
            ClientButton_RectangleMapS2I = new HashMap<>();

            for(int i=0;i<rec_list.length;i++){
                Rectangle base_rect = rec_list[i];
                Rectangle rect = new Rectangle((int)(base_rect.x*zoom),(int)(base_rect.y*zoom),(int)(base_rect.width*zoom),(int)(base_rect.height*zoom));
                int index = ClientButton_RectangleList.size();
                ClientButton_RectangleMapI2S.put(i,Button_Name_list[i]);
                ClientButton_RectangleMapS2I.put(Button_Name_list[i],i);
                ClientButton_RectangleList.add(rect);
                
                g.setColor(Color.WHITE);
                g.fillRect(rect.x, rect.y, rect.width, rect.height);
                g.setColor(Color.BLACK);
                g.drawRect(rect.x, rect.y, rect.width, rect.height);

                base = g.getFont();
                fontsize = (int)(50*zoom);
                fg = new Font("SansSerif" , Font.PLAIN , fontsize);
                g.setFont(fg);

                int tx = rect.x + (int)(10*zoom);
                int ty = rect.y+rect.height - (int)(20*zoom);
                g.drawString(Button_Name_list[i], tx, ty);
                
                g.setFont(base);
            }
            
            
        }
        
        //手番プレイヤーの表示
        base = g.getFont();
        fontsize = (int)(75*zoom);
        fg = new Font("SansSerif" , Font.PLAIN , fontsize);
        g.setFont(fg);
        x = (TextPoint.x)*zoom;
        y = (TextPoint.y)*zoom;
        String text = "Player" + this.currentPlayer +" is selecting the card.";
        g.drawString(text, (int)x, (int)y);
        g.setFont(base);
        //
        
        //操作に対するメッセージの表示
        base = g.getFont();
        fontsize = (int)(75*zoom);
        fg = new Font("SansSerif" , Font.PLAIN , fontsize);
        g.setFont(fg);
        x = (MessagePoint.x)*zoom;
        y = (MessagePoint.y)*zoom;
        g.drawString(this.uiMessage, (int)x, (int)y);
        g.setFont(base);
        //        
    }

    private void frameClicked(Point p){
        //System.out.println(p);
        //カードがクリックされたかの判定
        for(int i = 0;i<this.RectangleList.size();i++){
            Rectangle rect = this.RectangleList.get(i);
            if(rect.contains(p)){
                String placeID = this.RectangleMapI2S.get(i);
                this.cardClicked(placeID);
            }
        }
        //ボタンが押されたときの処理（クライアントモードの時のみ）
        if(this.clientMode){
            for(int i = 0;i<this.ClientButton_RectangleList.size();i++){
                Rectangle rect = this.ClientButton_RectangleList.get(i);
                if(rect.contains(p)){
                    String placeID = this.ClientButton_RectangleMapI2S.get(i);
                    this.clientButtonClicked(placeID);
                }
            }
        }
        this.validate();
        this.repaint();
    }
    
    private void clientButtonClicked(String buttonID){
        if(buttonID.equals(Button_Name_list[0])){
            //前進
            if(this.selectedRectangle.size() == 0){
                this.uiMessage = "Plase select a card.";
            } else if(this.selectedRectangle.size() > 1){
                this.uiMessage = "Plase select only 1 card.";
            } else {
                //１枚だけ選んでいる
                this.uiMessage = "Fowarding.";
                this.sendEvent(new ActionEvent(this,0,this.selectedRectangle.get(0)));
                this.selectedRectangle.clear();
            }
        } else if(buttonID.equals(Button_Name_list[1])){
            //後退
            if(this.selectedRectangle.size() == 0){
                this.uiMessage = "Plase select a card.";
            } else if(this.selectedRectangle.size() > 1){
                this.uiMessage = "Plase select only 1 card.";
            } else {
                //１枚だけ選んでいる
                this.uiMessage = "Backwarding.";
                this.sendEvent(new ActionEvent(this,1,this.selectedRectangle.get(0)));
                this.selectedRectangle.clear();
            }
            
        } else if(buttonID.equals(Button_Name_list[2])){
            //攻撃
            if(this.selectedRectangle.size() == 0){
                this.uiMessage = "Plase select one or more cards.";
            } else {
                //１枚以上選んでいる
                this.uiMessage = "Attacking.";
                
                this.sendEvent(new ActionEvent(this.selectedRectangle,2, this.selectedRectangle.get(0)));
                this.selectedRectangle.clear();
            }
        }
    }
    
    private void cardClicked(String placeID){
        System.out.println(placeID);
        if(this.selectedRectangle.contains(placeID)){
            this.selectedRectangle.remove(placeID);
        } else {
            this.selectedRectangle.add(placeID);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 691, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 293, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables


    private boolean drawGameImages = false;
    
    
    /** 個別に値をセット:プレイヤーの手札 */
    public synchronized void setPlayerHand(int playerID,ArrayList<Integer> cardList){
        drawGameImages = false;
        //値の変更
        setPlayerHand_innner(playerID,cardList);
        //
        drawGameImages = true;
        this.validate();
        this.repaint();
    }
    private void setPlayerHand_innner(int playerID,ArrayList<Integer> cardList){
        int[] clear = {-1,-1,-1,-1,-1};
        if(playerID == 0){
            this.player0Cards = clear;
            for(int i=0;i<cardList.size();i++){
                this.player0Cards[i] = cardList.get(i);
            }
        } else if(playerID == 1){
            this.player1Cards = clear;
            for(int i=0;i<cardList.size();i++){
                this.player1Cards[i] = cardList.get(i);
            }
        } else {
            System.err.println("UnKnown PlayerID:"+playerID);
        }
    }
    /** 個別に値をセット:デッキの枚数の手札 */
    public synchronized void setDeckCount(int count){
        drawGameImages = false;
        //値の変更
        setDeckCount_innner(count);
        //
        drawGameImages = true;
        this.validate();
        this.repaint();
    }
    private void setDeckCount_innner(int count){
        this.deck_count = count;
    }
    /** 個別に値をセット:プレイヤー位置 */
    public synchronized void setPlayerPosition(int playerID,int placeID){
        drawGameImages = false;
        //値の変更
        setPlayerPosition_innner(playerID,placeID);
        //
        drawGameImages = true;
        this.validate();
        this.repaint();
    }
    private void setPlayerPosition_innner(int playerID,int placeID){
        if(playerID == 0){
            this.player0_posion = placeID;
        } else if(playerID == 1){
            this.player1_posion = placeID;
        } else {
            System.err.println("UnKnown PlayerID:"+playerID);
        }
    }
    /** 個別に値をセット:プレイヤー点数 */
    public synchronized void setPlayerScore(int playerID,int score){
        drawGameImages = false;
        //値の変更
        setPlayerPosition_innner(playerID,score);
        //
        drawGameImages = true;
        this.validate();
        this.repaint();
    }
    private void setPlayerScore_innner(int playerID,int score){
        if(playerID == 0){
            this.player0_score = score;
        } else if(playerID == 1){
            this.player1_score = score;
        } else {
            System.err.println("UnKnown PlayerID:"+playerID);
        }
    }
        /** 個別に値をセット:デッキの枚数の手札 */
    public synchronized void setCurrentPlayer(int playerID){
        drawGameImages = false;
        //値の変更
        setCurrentPlayer_innner(playerID);
        //
        drawGameImages = true;
        this.validate();
        this.repaint();
    }
    private void setCurrentPlayer_innner(int playerID){
        this.currentPlayer = playerID;
    }
    /** まとめて値をセット */
    public synchronized void setDrawData(int currentPlayer,int player0Score,int player1Score,int player0Position,int player1Position,int deckCount,ArrayList<Integer> player0CardList,ArrayList<Integer> player1CardList){
        drawGameImages = false;
        //値の変更
        this.setCurrentPlayer_innner(currentPlayer);
        this.setPlayerHand_innner(0,player0CardList);
        this.setPlayerHand_innner(1,player1CardList);
        this.setDeckCount_innner(deckCount);
        this.setPlayerPosition_innner(0,player0Position);
        this.setPlayerPosition_innner(1,player1Position);
        this.setPlayerScore_innner(0,player0Score);
        this.setPlayerScore_innner(1,player1Score);
        //
        drawGameImages = true;
        this.validate();
        this.repaint();
    } 
    
    @Override
    public void paint(Graphics g){
        if(drawGameImages){
            this.drawImages();
        }
    }


}
