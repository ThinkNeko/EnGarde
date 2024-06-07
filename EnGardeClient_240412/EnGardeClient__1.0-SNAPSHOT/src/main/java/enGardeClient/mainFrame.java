/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package enGardeClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import engarde.gui.EnGardeGUI;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 *
 * @author ktajima
 */
public class mainFrame extends javax.swing.JFrame implements ActionListener {

    // 追加変数宣言.
    /*
     * 命名則
     * my or your:自分の情報か相手の情報かを明示。
     * rule:ゲーム進行のルールについて。
     */
    private int my_plan; // 作戦選択;
    private int my_attackdirection; // 0->1スタート、1->23スタート.
    private int my_hand[] = new int[5]; // 手札,1がn枚2がm枚・・・.
    private int my_matchcard[] = new int[2]; // 攻撃するカード候補、2個まで.
    private int my_matchnum; // 攻撃するカードの枚数の閾値.
    private int my_actioncard; // 出すカード(数字).
    private int my_actionid; // 101->移動、102->攻撃、103->パリー.
    private boolean my_movement; // F->Forward,T->Back.
    private int my_position; // 自身の現在地.

    private int your_position; // 相手の現在地.
    private int your_matchcard[] = new int[2]; // 相手が攻撃するカードの推測、2個まで.
    private List<Integer> your_matchnumlog = new ArrayList<>(); // 相手が攻撃してきた枚数の履歴
    private double your_matchaverage; // 相手が攻撃してきた枚数の平均.
    private int your_matchmin; // 相手が攻撃してきた枚数の最小値.

    private int rule_deck; // デッキの残りカード.
    private int rule_turn; // 何ターン目,1ターン目スタート.
    private int rule_distance; // 相手との距離.
    private int rule_cemetery[] = new int[5]; // 使用済みカード、墓地、0番目->1の使用済み枚数.
    private int rule_remain[] = new int[5]; // 残りカード.
    private List<Boolean> rule_issue = new ArrayList<>(Arrays.asList(true, false)); // 勝敗履歴 1->勝ち.
    private int rule_roundcount = 0; // 何試合目？.
    private int rule_win = 0; // 勝ち数.
    private int rule_lose = 0; // 負け数.

    // 追加変数宣言おわり.
    private String serverAddress;
    private int serverPort;

    // Socket版
    private Socket connectedSocket = null;
    private BufferedReader serverReader;
    private MessageReceiver receiver;
    private PrintWriter serverWriter;

    // 表示部分のドキュメントを管理するクラス
    private DefaultStyledDocument document_server;
    private DefaultStyledDocument document_system;

    // 追加メソッド開始.

    /** BoardInfoからデータを取得 */
    private void get_BoardInfo(HashMap<String, String> data) {
        my_position = Integer.parseInt(data.get("PlayerPosition_0"));
        your_position = Integer.parseInt(data.get("PlayerPosition_1"));
        rule_distance = Math.abs(my_position - your_position);
        rule_deck = Integer.parseInt(data.get("NumofDeck"));
        rule_turn = 16 - rule_deck;

        System.out.println("\n" + "\n" + (rule_turn) + "turn");
        System.out.println("get_BoardInfo");
        System.out.println("my_position:" + my_position + " , " + "your_position:" + your_position);
        System.out.println("print_cemetery");
        for (int i = 0; i < 5; i++) {
            System.out.print((i + 1) + ":" + rule_cemetery[i] + " ");
        }
        System.out.println();
    }

    /** HandInfoからデータを取得 */
    private void get_HandInfo(HashMap<String, String> data) {
        ArrayList<Integer> list = new ArrayList<>();
        String[] keys = { "Hand1", "Hand2", "Hand3", "Hand4", "Hand5" };
        for (String key : keys) {
            if (data.containsKey(key)) {
                list.add(Integer.parseInt(data.get(key)));
            }
        }
        // list->配列.
        // 配列のリセット.
        for (int i = 0; i < 5; i++) {
            my_hand[i] = 0;
        }
        // 1がn枚2がm枚の配列に.
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == 1) {
                my_hand[0] = my_hand[0] + 1;
            } else if (list.get(i) == 2) {
                my_hand[1] = my_hand[1] + 1;
            } else if (list.get(i) == 3) {
                my_hand[2] = my_hand[2] + 1;
            } else if (list.get(i) == 4) {
                my_hand[3] = my_hand[3] + 1;
            } else if (list.get(i) == 5) {
                my_hand[4] = my_hand[4] + 1;
            }
        }
        System.out.println("get_HandInfo");
        System.out.println(my_hand[0] + " , " + my_hand[1] + " , " + my_hand[2] + " , " + my_hand[3] + " , "
                + my_hand[4]);
    }

    /** DoPlayからデータを取得 */
    private void get_DoPlay(HashMap<String, String> data) {
        if (Integer.parseInt(data.get("MessageID")) == 101) {

        } else if (Integer.parseInt(data.get("MessageID")) == 102) {

        }
    }

    /**
     * RoundEndからデータを取得
     * 試合結果から学習
     */
    private void get_RoundEnd(HashMap<String, String> data) {
        rule_roundcount = rule_roundcount + 1;
        rule_issue.add(Boolean.parseBoolean(data.get("Winner")));
        if (my_attackdirection == 0) {
            rule_win = Integer.parseInt(data.get("Score0"));
            rule_lose = Integer.parseInt(data.get("Score1"));
        } else if (my_attackdirection == 1) {
            rule_win = Integer.parseInt(data.get("Score1"));
            rule_lose = Integer.parseInt(data.get("Score0"));
        }
        // 墓地リセット.
        for (int i = 0; i < 5; i++) {
            rule_cemetery[i] = 0;
        }
        System.out.println("\n" + rule_roundcount + "end");
        System.out.println("average : " + your_matchaverage);
    }

    private void get_GameEnd(HashMap<String, String> data) {

    }

    /** Playedからデータを取得 */
    private void get_Played(HashMap<String, String> data) {
        // 墓地実装.
        if (Integer.parseInt(data.get("MessageID")) == 102) {
            rule_cemetery[Integer.parseInt(data.get("PlayCard"))
                    - 1] = rule_cemetery[Integer.parseInt(data.get("PlayCard")) - 1]
                            + Integer.parseInt(data.get("NumOfCard")) * 2;
            your_matchnumlog.add(Integer.parseInt(data.get("NumOfCard"))); // 攻撃枚数履歴追加.
            your_matchmin = Collections.min(your_matchnumlog);
            // 平均計算.
            double sum = 0;
            for (int i = 0; i < your_matchnumlog.size(); i++) {
                sum = sum + your_matchnumlog.get(i);
            }
            your_matchaverage = sum / your_matchnumlog.size();
        } else if (Integer.parseInt(data.get("MessageID")) == 101) {
            rule_cemetery[Integer.parseInt(data.get("PlayCard"))
                    - 1] = rule_cemetery[Integer.parseInt(data.get("PlayCard")) - 1] + 1;
        }
        for (int i = 0; i < 4; i++) {
            rule_remain[i] = 5 - rule_cemetery[i];
        }

    }

    private void algorithm_player0() {
        // 大きい順に出す.
        // 評価値送信.
        /*
         * while (true) {
         * try {
         * sendEvaluateMessage();
         * System.out.println("sendEvaluateMessage");
         * break;
         * } catch (IOException | InterruptedException e) {
         * System.out.println("Error:sendEvaluateMessage");
         * }
         * }
         */

        // 作戦決定.
        // 4ターン目(自分が2回行動する)までに作戦決定する.
        if (rule_turn <= 2) {
            // 一回目はとりあえず最大値前進.
            my_plan = 1;
        } else if (rule_turn <= 4) {
            if ((my_position >= 5 && my_hand[3] + my_hand[4] >= 1) || my_hand[3] + my_hand[4] >= 2) {
                /*
                 * 序盤で中央まで近づく.
                 * 
                 */
                my_plan = 1;
            } else {
                /*
                 * 序盤に進めれない.
                 * 
                 */
                my_plan = 2;
            }
        }
        System.out.println("plan:" + my_plan);

        // 勝負するカード決定.
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0, my_hand[0]);
        map.put(1, my_hand[1]);
        map.put(2, my_hand[2]);
        map.put(3, my_hand[3]);
        map.put(4, my_hand[4]);
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
        my_matchcard[0] = list.get(0).getKey(); // 第1候補.
        my_matchcard[1] = list.get(1).getKey(); // 第2候補.
        System.out.println("my_matchcard : " + (my_matchcard[0] + 1) + " or " + (my_matchcard[1] + 1));

        // 相手の勝負するカードの推定.
        Map<Integer, Integer> map1 = new HashMap<>();
        map1.put(0, rule_remain[0]);
        map1.put(1, rule_remain[1]);
        map1.put(2, rule_remain[2]);
        map1.put(3, rule_remain[3]);
        map1.put(4, rule_remain[4]);
        List<Map.Entry<Integer, Integer>> list1 = new ArrayList<>(map1.entrySet());
        list1.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
        your_matchcard[0] = list1.get(0).getKey(); // 第1候補.
        your_matchcard[1] = list1.get(1).getKey(); // 第2候補.
        System.out.println("your_matchcard : " + (your_matchcard[0] + 1) + " or " + (your_matchcard[1] + 1));

        if (my_plan == 0) {

        } else if (my_plan == 1 || my_plan == 2) {
            boolean canForwardorAttack = false; // 前進か攻撃できるか？.
            for (int i = 4; i >= 0; i--) {
                if (my_hand[i] != 0) {
                    if (i + 1 == rule_distance) {
                        if (((i == my_matchcard[0] || i == my_matchcard[1])
                                && (my_hand[i] >= your_matchaverage))
                                || my_hand[0] + my_hand[1] + my_hand[2]
                                        + my_hand[3] + my_hand[4] != 5) {
                            // 攻撃
                            my_actioncard = i + 1;
                            my_actionid = 102;
                            System.out.println("attack:" + my_actioncard);
                            canForwardorAttack = true;

                            break;
                        }
                    } else if (i + 1 < rule_distance) {
                        // 前進.
                        if ((rule_distance - (i + 1) != your_matchcard[0] || rule_distance
                                - (i + 1) != your_matchcard[1]) || my_hand[i] > your_matchaverage) {
                            my_actioncard = i + 1;
                            my_actionid = 101;
                            my_movement = false;
                            System.out.println("movement:" + my_actioncard);
                            canForwardorAttack = true;
                            break;
                        }

                    } else {
                        System.out.println("miss:" + i);
                    }
                }
            }
            if (canForwardorAttack == false) {
                for (int i = 0; i < 5; i++) {
                    if (my_hand[i] != 0) {
                        // 後退.
                        if (rule_distance - (i + 1) != your_matchcard[0] || rule_distance
                                - (i + 1) != your_matchcard[1]) {
                            my_actioncard = i + 1;
                            my_actionid = 101;
                            my_movement = true;
                            System.out.println("movement:" + my_actioncard);
                            break;
                        }
                    }
                }
            }
        } else if (my_plan == 2) {

        }

    }

    private void algorithm_player1() {
        // 大きい順に出す
        // 評価値送信.
        /*
         * while (true) {
         * try {
         * sendEvaluateMessage();
         * System.out.println("sendEvaluateMessage");
         * break;
         * } catch (IOException | InterruptedException e) {
         * System.out.println("Error:sendEvaluateMessage");
         * }
         * }
         */
        // 作戦決定.
        // 4ターン目(自分が2回行動する)までに作戦決定する.
        if (rule_turn <= 2) {
            // 一回目はとりあえず最大値前進.
            my_plan = 1;
        } else if (rule_turn <= 4) {
            if ((my_position <= 19 && my_hand[3] + my_hand[4] >= 1) || my_hand[3] + my_hand[4] >= 2) {
                /*
                 * 序盤で中央まで近づく.
                 * 
                 */
                my_plan = 1;
            } else {
                /*
                 * 序盤に進めれない.
                 * 
                 */
                my_plan = 2;
            }
        }
        System.out.println("plan:" + my_plan);

        // 勝負するカード決定.
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0, my_hand[0]);
        map.put(1, my_hand[1]);
        map.put(2, my_hand[2]);
        map.put(3, my_hand[3]);
        map.put(4, my_hand[4]);
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
        my_matchcard[0] = list.get(0).getKey(); // 第1候補.
        my_matchcard[1] = list.get(1).getKey(); // 第2候補.
        System.out.println("my_matchcard : " + (my_matchcard[0] + 1) + " or " + (my_matchcard[1] + 1));

        // 相手の勝負するカードの推定.
        Map<Integer, Integer> map1 = new HashMap<>();
        map1.put(0, rule_remain[0]);
        map1.put(1, rule_remain[1]);
        map1.put(2, rule_remain[2]);
        map1.put(3, rule_remain[3]);
        map1.put(4, rule_remain[4]);
        List<Map.Entry<Integer, Integer>> list1 = new ArrayList<>(map1.entrySet());
        list1.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
        your_matchcard[0] = list1.get(0).getKey(); // 第1候補.
        your_matchcard[1] = list1.get(1).getKey(); // 第2候補.
        System.out.println("your_matchcard : " + (your_matchcard[0] + 1) + " or " + (your_matchcard[1] + 1));

        if (my_plan == 0) {

        } else if (my_plan == 1 || my_plan == 2) {
            boolean canForwardorAttack = false; // 前進か攻撃できるか？.
            for (int i = 4; i >= 0; i--) {
                if (my_hand[i] != 0) {
                    if (i + 1 == rule_distance) {
                        if (((i == my_matchcard[0] || i == my_matchcard[1])
                                && (my_hand[i] >= your_matchaverage))
                                || my_hand[0] + my_hand[1] + my_hand[2]
                                        + my_hand[3] + my_hand[4] != 5) {
                            // 攻撃
                            my_actioncard = i + 1;
                            my_actionid = 102;
                            System.out.println("attack:" + my_actioncard);
                            canForwardorAttack = true;

                            break;
                        }
                    } else if (i + 1 < rule_distance) {
                        // 前進.
                        if ((rule_distance - (i + 1) != your_matchcard[0] || rule_distance
                                - (i + 1) != your_matchcard[1]) || my_hand[i] > your_matchaverage) {
                            my_actioncard = i + 1;
                            my_actionid = 101;
                            my_movement = false;
                            System.out.println("movement:" + my_actioncard);
                            canForwardorAttack = true;
                            break;
                        }

                    } else {
                        System.out.println("miss:" + i);
                    }
                }
            }
            if (canForwardorAttack == false) {
                for (int i = 0; i < 5; i++) {
                    if (my_hand[i] != 0) {
                        // 後退.
                        if (rule_distance - (i + 1) != your_matchcard[0] || rule_distance
                                - (i + 1) != your_matchcard[1]) {
                            my_actioncard = i + 1;
                            my_actionid = 101;
                            my_movement = true;
                            System.out.println("movement:" + my_actioncard);
                            break;
                        }
                    }
                }
            }
        } else if (my_plan == 2) {

        }
    }

    private void send_Play() {
        if (my_actionid == 101) {
            if (my_movement == true) {
                // 後退.
                try {
                    sendBackwardMessage(my_actioncard);
                    System.out.println("Back : " + my_actioncard);
                    // 墓地.
                    rule_cemetery[my_actioncard - 1] = rule_cemetery[my_actioncard - 1] + 1;
                } catch (IOException | InterruptedException e) {
                    System.out.println("Error:sendBackwardMessage");
                }
            } else if (my_movement == false) {
                // 前進.
                try {
                    sendForwardMessage(my_actioncard);
                    System.out.println("Forward : " + my_actioncard);
                    // 墓地.
                    rule_cemetery[my_actioncard - 1] = rule_cemetery[my_actioncard - 1] + 1;
                } catch (IOException | InterruptedException e) {
                    System.out.println("Error:sendForwardMessage");
                }
            }

        } else if (my_actionid == 102) {
            try {
                sendAttackMessage(my_actioncard, my_hand[my_actioncard - 1]);
                System.out.println("Attack : " + my_actioncard + "," + my_hand[my_actioncard - 1]);
                // 墓地.
                rule_cemetery[my_actioncard - 1] = rule_cemetery[my_actioncard - 1] + my_hand[my_actioncard - 1] * 2;
            } catch (IOException | InterruptedException e) {
                System.out.println("Error:sendAttackMessage");
            }
        } else {

        }
    }

    /** エラー処理 */
    private void error_processing(HashMap<String, String> data) {
        String type = data.get("MessageID");
        switch (type) {
            case "404":
                boolean canForwardorAttack = false; // 前進か攻撃できるか？.
                for (int i = 4; i >= 0; i--) {
                    if (my_hand[i] != 0) {
                        if (i + 1 == rule_distance) {
                            // 攻撃
                            my_actioncard = i + 1;
                            my_actionid = 102;
                            System.out.println("attack:" + my_actioncard);
                            canForwardorAttack = true;
                            break;
                        } else if (i + 1 < rule_distance) {
                            // 前進.
                            my_actioncard = i + 1;
                            my_actionid = 101;
                            my_movement = false;
                            System.out.println("movement:" + my_actioncard);
                            canForwardorAttack = true;
                            break;
                        } else {
                            System.out.println("miss:" + i);
                        }
                    }
                }
                if (canForwardorAttack == false) {
                    for (int i = 0; i < 5; i++) {
                        if (my_hand[i] != 0) {
                            // 後退.
                            my_actioncard = i + 1;
                            my_actionid = 101;
                            my_movement = true;
                            System.out.println("movement:" + my_actioncard);
                            break;
                        }
                    }
                }
                break;
            default:
        }

    }
    // 追加メソッド終了.

    /** HashMapを送るメソッド */

    private void sendMassageWithSocket(HashMap<String, String> data) throws IOException, InterruptedException {
        StringBuilder response = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper();
        // response.append("<json>");
        response.append(mapper.writeValueAsString(data));
        // response.append("</json>");
        this.serverWriter.println(response.toString());
        this.serverWriter.flush();
        // DEBUG
        this.printMessage("[Sent]" + data.toString());
        // DEBUG
    }

    /** 指定した文字列を送るメソッド */
    private void sendMassageWithSocket(String message) throws IOException, InterruptedException {
        this.serverWriter.println(message);
        this.serverWriter.flush();
        // DEBUG
        this.printMessage("[Sent]" + message);
        // DEBUG
    }

    private void connectToServer() {
        this.serverAddress = this.jTextField1.getText();
        this.serverPort = Integer.parseInt(this.jTextField2.getText());

        // Socket版
        try {
            this.connectedSocket = new Socket(this.serverAddress, this.serverPort);
            this.serverReader = new BufferedReader(new InputStreamReader(connectedSocket.getInputStream()));
            this.serverWriter = new PrintWriter(new OutputStreamWriter(connectedSocket.getOutputStream()));

            this.receiver = new MessageReceiver(this);
            this.receiver.start();
            this.printMessage("サーバに接続しました。");

        } catch (IOException ex) {
            this.printMessage("サーバに接続できませんでした。");
            Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * スレッドに読み込みを行わせる用の取り出しメソッド
     * 
     * @return
     */
    public BufferedReader getServerReader() {
        return this.serverReader;
    }

    /**
     * スレッドから読みこんだメッセージを受信するメソッド
     * 
     * @param message
     */
    public void receiveMessageFromServer(String message) {
        this.showRecivedMessage(message);
        try {
            // JSON -> HashMAP
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, String> de_map = mapper.readValue(message, HashMap.class);
            this.receiveDataFromServer(de_map);

        } catch (JsonProcessingException ex) {
        }
    }

    /**
     * GUI上に受信したメッセージを表示するメソッド
     * 
     * @param message
     */
    public void showRecivedMessage(String message) {
        try {
            SimpleAttributeSet attribute = new SimpleAttributeSet();
            attribute.addAttribute(StyleConstants.Foreground, Color.BLACK);
            // ドキュメントにその属性情報つきの文字列を挿入
            document_server.insertString(document_server.getLength(), message + "\n", attribute);
            this.jTextArea1.setCaretPosition(document_server.getLength());

        } catch (BadLocationException ex) {
        }
    }

    /**
     * GUI上にシステムメッセージを表示するメソッド
     * 
     * @param message
     */
    public void printMessage(String message) {
        System.out.println(message);
        try {
            SimpleAttributeSet attribute = new SimpleAttributeSet();
            attribute.addAttribute(StyleConstants.Foreground, Color.BLACK);
            // ドキュメントにその属性情報つきの文字列を挿入
            document_system.insertString(document_system.getLength(), message + "\n", attribute);
            this.jTextArea3.setCaretPosition(document_system.getLength());

        } catch (BadLocationException ex) {
        }
    }

    private int myPlayerID = -1;

    /**
     * データハンドリングメソッド
     * 
     * @param data
     */
    public void receiveDataFromServer(HashMap<String, String> data) {
        if (!data.containsKey("Type")) {
            return;
        }
        try {
            String type = data.get("Type");
            switch (type) {
                case "ConnectionStart":
                    // 自分の番号の確定
                    this.myPlayerID = Integer.parseInt(data.get("ClientID"));
                    // なまえの入力
                    this.sendMyName();
                    this.view = new EnGardeGUI();
                    this.view.addEventListener(this);

                    // 自身の攻め方向の決定.
                    my_attackdirection = Integer.parseInt(data.get("ClientID"));

                    break;
                case "NameReceived":
                    break;
                case "BoardInfo":
                    // メインボード情報.
                    this.showBoard(data);

                    get_BoardInfo(data);
                    break;
                case "HandInfo":
                    // 手札のカードの情報.
                    this.showHand(data);

                    get_HandInfo(data);
                    break;
                case "DoPlay":
                    // プレイヤーのターンが来たことを知らせる.
                    get_DoPlay(data);
                    if (my_attackdirection == 0) {
                        algorithm_player0();
                    } else if (my_attackdirection == 1) {
                        algorithm_player1();
                    }
                    break;
                case "RoundEnd":
                    // ラウンド終了.
                    get_RoundEnd(data);
                    break;
                case "GameEnd":
                    get_GameEnd(data);
                    break;
                case "Accept":
                    if (Integer.parseInt(data.get("MessageID")) == 200) {
                        send_Play();
                    }
                    break;
                case "Played":
                    get_Played(data);
                    break;
                case "Error":
                    error_processing(data);
                    break;
                default:
            }
        } catch (IOException ex) {
            Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private EnGardeGUI view;
    private ArrayList<Integer> myCardList = new ArrayList<>();

    /** 現在のボードの描画 */
    private void showBoard(HashMap<String, String> data) {
        // {"Type":"BoardInfo","PlayerPosition_1":"23","NumofDeck":"15","CurrentPlayer":"0","PlayerPosition_0":"1","From":"Server","To":"Client","PlayerScore_0":"0","PlayerScore_1":"0"}
        int currentPlayer = Integer.parseInt(data.get("CurrentPlayer"));
        int player0Score = Integer.parseInt(data.get("PlayerScore_0"));
        int player1Score = Integer.parseInt(data.get("PlayerScore_1"));
        int player0Position = Integer.parseInt(data.get("PlayerPosition_0"));
        int player1Position = Integer.parseInt(data.get("PlayerPosition_1"));
        int DeckCount = Integer.parseInt(data.get("NumofDeck"));
        this.view.setDrawData(currentPlayer, player0Score, player1Score, player0Position, player1Position, DeckCount,
                new ArrayList<Integer>(), new ArrayList<Integer>());
        this.view.setPlayerHand(myPlayerID, myCardList);
        this.view.setVisible(true);
    }

    /** 手札の状況 */
    private void showHand(HashMap<String, String> data) {
        // {Hand3=1, Hand4=3, Type=HandInfo, Hand5=1, Hand1=5, Hand2=4, From=Server,
        // To=Client}
        this.myCardList.clear();
        String[] keys = { "Hand1", "Hand2", "Hand3", "Hand4", "Hand5" };
        for (String key : keys) {
            if (data.containsKey(key)) {
                this.myCardList.add(Integer.parseInt(data.get(key)));
            }
        }
        this.view.setPlayerHand(myPlayerID, myCardList);
        try {
            this.sendEvaluateMessage();
        } catch (IOException ex) {
            Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.view.setVisible(true);
    }

    private void sendMyName() throws IOException, InterruptedException {
        // 固定のプレイヤー名
        String PlayerName = "YasudaLabo";

        // JSON構成
        // likes
        // <json>{"Type":"PlayerName","From":"Client","To":"Server","Name":"FixedPlayerName"}</json>
        HashMap<String, String> response = new HashMap<>();
        response.put("From", "Client");
        response.put("To", "Server");
        response.put("Type", "PlayerName");
        response.put("Name", PlayerName);

        StringBuilder sbuf = new StringBuilder();
        try {
            ObjectMapper mapper = new ObjectMapper();
            // sbuf.append("<json>"); // コメントアウトされた行
            sbuf.append(mapper.writeValueAsString(response));
            // sbuf.append("</json>"); // コメントアウトされた行
        } catch (JsonProcessingException ex) {
            Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (sbuf.length() > 0) {
            this.sendMassageWithSocket(sbuf.toString());
        }
    }

    private void sendForwardMessage(int cardNumber) throws IOException, InterruptedException {
        if (!this.myCardList.contains(cardNumber)) {
            return;
        }
        // JSON構成
        // likes
        // <json>{"Type":"PlayerName","From":"Client","To":"Server","Name":"Simple"}</json>
        HashMap<String, String> response = new HashMap<>();
        response.put("From", "Client");
        response.put("To", "Server");
        response.put("Type", "Play");
        response.put("Direction", "F");
        response.put("MessageID", "101");
        response.put("PlayCard", Integer.toString(cardNumber));

        StringBuilder sbuf = new StringBuilder();
        try {
            ObjectMapper mapper = new ObjectMapper();
            // sbuf.append("<json>");
            sbuf.append(mapper.writeValueAsString(response));
            // sbuf.append("</json>");
        } catch (JsonProcessingException ex) {
            Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (sbuf.length() > 0) {
            this.sendMassageWithSocket(sbuf.toString());
        }
    }

    private void sendBackwardMessage(int cardNumber) throws IOException, InterruptedException {
        if (!this.myCardList.contains(cardNumber)) {
            return;
        }
        // JSON構成
        // likes
        // <json>{"Type":"PlayerName","From":"Client","To":"Server","Name":"Simple"}</json>
        HashMap<String, String> response = new HashMap<>();
        response.put("From", "Client");
        response.put("To", "Server");
        response.put("Type", "Play");
        response.put("Direction", "B");
        response.put("MessageID", "101");
        response.put("PlayCard", Integer.toString(cardNumber));

        StringBuilder sbuf = new StringBuilder();
        try {
            ObjectMapper mapper = new ObjectMapper();
            // sbuf.append("<json>");
            sbuf.append(mapper.writeValueAsString(response));
            // sbuf.append("</json>");
        } catch (JsonProcessingException ex) {
            Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (sbuf.length() > 0) {
            this.sendMassageWithSocket(sbuf.toString());
        }
    }

    private void sendAttackMessage(int cardNumber, int cardCount) throws IOException, InterruptedException {
        if (!this.myCardList.contains(cardNumber)) {
            return;
        }
        // JSON構成
        // <json>{"Type":"Play","PlayCard":"4","From":"Client","To":"Server","NumOfCard":"2","MessageID":"102"}</json>
        HashMap<String, String> response = new HashMap<>();
        response.put("From", "Client");
        response.put("To", "Server");
        response.put("Type", "Play");
        response.put("NumOfCard", "B");
        response.put("MessageID", "102");
        response.put("NumOfCard", Integer.toString(cardCount));
        response.put("PlayCard", Integer.toString(cardNumber));

        StringBuilder sbuf = new StringBuilder();
        try {
            ObjectMapper mapper = new ObjectMapper();
            // sbuf.append("<json>");
            sbuf.append(mapper.writeValueAsString(response));
            // sbuf.append("</json>");
        } catch (JsonProcessingException ex) {
            Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (sbuf.length() > 0) {
            this.sendMassageWithSocket(sbuf.toString());
        }
    }

    /** このメソッドは単に固定値を送るだけ */
    private void sendEvaluateMessage() throws IOException, InterruptedException {
        // JSON構成
        // <json>{"Type":"Evaluation","PlayCard":"4","From":"Client","To":"Server","NumOfCard":"2","MessageID":"102"}</json>
        HashMap<String, String> response = new HashMap<>();
        response.put("From", "Client");
        response.put("To", "Server");
        response.put("Type", "Evaluation");
        response.put("1F", "0.1");
        response.put("2F", "0.1");
        response.put("3F", "0.1");
        response.put("4F", "0.1");
        response.put("5F", "0.1");
        response.put("1B", "0.1");
        response.put("2B", "0.1");
        response.put("3B", "0.1");
        response.put("4B", "0.1");
        response.put("5B", "0.1");

        StringBuilder sbuf = new StringBuilder();
        try {
            ObjectMapper mapper = new ObjectMapper();
            // sbuf.append("<json>");
            sbuf.append(mapper.writeValueAsString(response));
            // sbuf.append("</json>");
        } catch (JsonProcessingException ex) {
            Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (sbuf.length() > 0) {
            this.sendMassageWithSocket(sbuf.toString());
        }
    }

    /**
     * Creates new form mainFrame
     */
    public mainFrame() {
        initComponents();
        this.document_server = new DefaultStyledDocument();
        this.document_system = new DefaultStyledDocument();

        this.jTextArea1.setDocument(this.document_server);
        this.jTextArea3.setDocument(this.document_system);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("2024 情報工学実験 En Garde GUI Sever 1.0");

        jTextField1.setText("localhost");

        jLabel2.setText("ServerIP");

        jLabel3.setText("Port");

        jTextField2.setText("12052");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jButton1.setText("Connect");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel4.setText("Recived");

        jLabel5.setText("Send JSON Data");

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        jButton2.setText("Send");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Make");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel6.setText("SystemMessage");

        jTextArea3.setColumns(20);
        jTextArea3.setRows(5);
        jScrollPane3.setViewportView(jTextArea3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                        layout.createSequentialGroup()
                                                                .addComponent(jLabel4)
                                                                .addGap(0, 327, Short.MAX_VALUE))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addGroup(layout
                                                                .createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.TRAILING,
                                                                        false)
                                                                .addComponent(jLabel3,
                                                                        javax.swing.GroupLayout.Alignment.LEADING,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        Short.MAX_VALUE)
                                                                .addComponent(jLabel2,
                                                                        javax.swing.GroupLayout.Alignment.LEADING,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        Short.MAX_VALUE))
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addGroup(layout
                                                                .createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.LEADING,
                                                                        false)
                                                                .addComponent(jTextField1)
                                                                .addComponent(jTextField2,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE, 318,
                                                                        Short.MAX_VALUE))))
                                        .addComponent(jLabel5)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(jButton1)
                                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 366,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jLabel6)
                                        .addGroup(layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(jButton3)
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jButton2))
                                                .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE))
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 266,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(14, Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel3)
                                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(9, 9, 9)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(jButton1))
                                .addGap(4, 4, 4)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 122,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 58,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton2)
                                        .addComponent(jButton3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 50,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(15, Short.MAX_VALUE)));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton3ActionPerformed
        JsonMaker dialog = new JsonMaker(this, false);
        dialog.setVisible(true);
        dialog.setMainFrame(this);
    }// GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed
        this.connectToServer();
    }// GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton2ActionPerformed
        try {
            this.sendMassageWithSocket(this.jTextArea2.getText());
        } catch (IOException ex) {
            Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.jTextArea2.setText("");
    }// GEN-LAST:event_jButton2ActionPerformed

    /** サブウインドウからの変更要求 */
    public void setSendText(String message) {
        this.jTextArea2.setText(message);
    }

    private int getCardID(String command) {
        int cardID = -1;
        if (command.equals("Player0_Card1")) {
            cardID = 0;
        } else if (command.equals("Player0_Card2")) {
            cardID = 1;
        } else if (command.equals("Player0_Card3")) {
            cardID = 2;
        } else if (command.equals("Player0_Card4")) {
            cardID = 3;
        } else if (command.equals("Player0_Card5")) {
            cardID = 4;
        }
        if (command.equals("Player1_Card1")) {
            cardID = 0;
        } else if (command.equals("Player1_Card2")) {
            cardID = 1;
        } else if (command.equals("Player1_Card3")) {
            cardID = 2;
        } else if (command.equals("Player1_Card4")) {
            cardID = 3;
        } else if (command.equals("Player1_Card5")) {
            cardID = 4;
        }
        return cardID;
    }

    /** GUIからのrequest処理 */
    @Override
    public void actionPerformed(ActionEvent e) {
        int type_id = e.getID();
        String command = e.getActionCommand();
        int cardID = -1;
        switch (type_id) {
            case 0:
                // 前進ボタン
                cardID = getCardID(command);
                if (cardID != -1) {
                    try {
                        int cardNum = this.myCardList.get(cardID);
                        this.sendForwardMessage(cardNum);
                    } catch (IOException ex) {
                        Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            case 1:
                // 後退ボタン
                cardID = getCardID(command);
                if (cardID != -1) {
                    try {
                        int cardNum = this.myCardList.get(cardID);
                        this.sendBackwardMessage(cardNum);
                    } catch (IOException ex) {
                        Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            case 2:
                // 攻撃ボタン
                ArrayList<String> selectedList = (ArrayList<String>) e.getSource();
                int cardNum = -1;
                int cardCount = 0;
                for (String cmd : selectedList) {
                    cardID = getCardID(cmd);
                    int num = this.myCardList.get(cardID);
                    if (cardNum == -1) {
                        cardNum = num;
                        cardCount = 1;
                    } else if (cardNum == num) {
                        cardCount++;
                    }
                }
                try {
                    this.sendAttackMessage(cardNum, cardCount);
                } catch (IOException ex) {
                    Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }

                break;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables

}

class MessageReceiver extends Thread {
    private mainFrame parent;
    private BufferedReader serverReader;
    private StringBuilder sbuf;

    // public static Pattern jsonStartEnd =
    // Pattern.compile(".*?<json>(.*)</json>.*?");
    // public static Pattern jsonEnd = Pattern.compile("(.*)</json>.*?");
    // public static Pattern jsonStart = Pattern.compile(".*?<json>(.*)");
    public static Pattern jsonStartEnd = Pattern.compile(".*?\\{(.*)\\}.*?");
    public static Pattern jsonEnd = Pattern.compile("(.*)\\}.*?");
    public static Pattern jsonStart = Pattern.compile(".*?\\{(.*)");

    public MessageReceiver(mainFrame p) {
        this.parent = p;
        this.serverReader = this.parent.getServerReader();
        this.sbuf = new StringBuilder();
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = this.serverReader.readLine()) != null) {
                Matcher startend = jsonStartEnd.matcher(line);
                Matcher end = jsonEnd.matcher(line);
                Matcher start = jsonStart.matcher(line);
                if (startend.matches()) {
                    sbuf = new StringBuilder();
                    sbuf.append("{");
                    sbuf.append(startend.group(1));
                    sbuf.append("}{");
                    this.parent.receiveMessageFromServer(sbuf.toString());

                } else if (end.matches()) {
                    sbuf.append(end.group(1));
                    sbuf.append("}{");
                    this.parent.receiveMessageFromServer(sbuf.toString());

                } else if (start.matches()) {
                    sbuf = new StringBuilder();
                    sbuf.append("{");
                    sbuf.append(start.group(1));

                } else {
                    sbuf.append(line);

                }
            }
        } catch (IOException ex) {
        }
    }

}
