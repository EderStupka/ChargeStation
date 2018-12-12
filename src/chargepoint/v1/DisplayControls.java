package chargepoint.v1;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;
import java.util.concurrent.TimeUnit ;
import java.util.logging.Logger;
import javax.websocket.DeploymentException;
import javax.websocket.Session;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author pi
 */
public class DisplayControls extends javax.swing.JFrame {

    final static CountDownLatch messageLatch = new CountDownLatch(1);

    /**
     * Creates new form DisplayControls
     */
    public DisplayControls() {
        initComponents();
        
    }
    
    public static void main(String[] args) throws URISyntaxException {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DisplayControls.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DisplayControls.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DisplayControls.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DisplayControls.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DisplayControls().setVisible(true);
            }
        });
        
        
       
        try {
            
            StartConnection();
        } catch (InterruptedException ex) {
            System.err.println("InterruptedException exception: " + ex.getMessage());
        }
        
    }
    
    
    public static void StartConnection() throws InterruptedException {
        
        boolean connection = true; 
        while (connection){
            clientEndPoint = null;
            try{
                System.out.println("Entrou no try");
                Socket socket = new Socket("www.google.com", 80);
                boolean netAccess = socket.isConnected();
                socket.close();
                if(netAccess){
                    System.out.println("Conectado");
                    Thread.sleep(2000);
                    clientEndPoint = new WebsocketClientEndpoint(new URI("ws://192.168.2.102:8000"));
                    connection = false;
                    BootNotification();
                }
            }
            catch(java.net.MalformedURLException urlmal){
                System.err.println("INFO 1: Sem conexão com a internet\n");
                DisplayControls.LogTextArea.append("INFO 1: Sem conexão com a internet\n");
                connection = false;
                Thread.sleep(10000);
                StartConnection();
            }
            catch(java.io.IOException ioexcp){
                System.err.println("INFO 2: Sem conexão com a internet\n");
                //DisplayControls.LogTextArea.append("INFO 2: Sem conexão com a internet\n");
                connection = false;
                Thread.sleep(10000);
                StartConnection();
            } catch (RuntimeException ex) {
                System.err.println("INFO 3: Falha na conexão websocket\n");
                DisplayControls.LogTextArea.append("INFO 3: Falha na conexão websocket\n");
                connection = false;
                Thread.sleep(5000);
                StartConnection();
            } catch (URISyntaxException ex) {
                System.err.println("URISyntaxException exception: " + ex.getMessage());
            }
            Thread.sleep(1000);
        }
    }
   
    
    
    private static void BootNotification() throws InterruptedException, IOException {
        
        MessageId = getDateTime();
        
             
        String Request = "[{2,\""+MessageId+"\",\"BootNotification\",{\"reason\": \"PowerUp\", \"chargingStation\": {\"serialNumber\":\""+SerialNumber.getText()+"\", \"model\": \""+Model.getText()+"\",\"vendorName\":\""+VendorName.getText()+"\", \"firmwareVersion\":\""+FirmwareVersion.getText()+"\"}}]\n";
        
        //try{
        //    ValidateJSON.ValidateBootNotification(Request);
           
        //}catch(IOException ex){
        //    System.err.println("Falha no JSON\n");
        //}
        
        
        String Response = Communication(Request);

        if (Response.contains("BootNotification")){
            if (Response.contains("Accepted")){
                System.out.println("BOOTNOTIFICATION OK");
                IdleStatus();
            }
            if (Response.contains("Rejected")){
                System.out.println("BOOTNOTIFICATION NOK");
                 Thread.sleep(5000);
                 BootNotification();
            }
        }
    }
    
    private static void IdleStatus() throws InterruptedException{
        
        Idle = true;
        
        //while(Idle){
            
            System.out.println("Aguardando autorização!"+AuthorizationRequest+" - "+CabbleConnected);
            if (AuthorizationRequest && CabbleConnected){
                String Action = "Update";
                String Reason = "Authorized";
                String Response = TransactionEvent(Action, Reason);
                if (Response.contains("TransactionEvent")){
                    if (Response.contains("Accepted")){
                        System.out.println("Iniciar Carga OK\n");
                        DisplayControls.LogTextArea.append("INFO: INICIO DA GARGA\n");
                        DisplayControls.ChargeStatus.setBackground(Color.green);
                        
                    }
                    else{
                        System.out.println("Iniciar Carga NOK");
                        Thread.sleep(2000);
                        
                    }
                }
            }
            else{
                DisplayControls.ChargeStatus.setBackground(Color.red);
            }
        //}
    }
    
    public static void AuthorizationRequest() throws InterruptedException{
        MessageId = getDateTime();
        
        String Request = "[{2,\""+MessageId+"\",\"AuthorizationRequest\",{\"UserID\": \""+UserID.getText()+"\", \"chargingStation\": {\"serialNumber\":\""+SerialNumber.getText()+"\", \"model\": \""+Model.getText()+"\",\"vendorName\":\""+VendorName.getText()+"\", \"firmwareVersion\":\""+FirmwareVersion.getText()+"\"}}]\n";
        String Response = Communication(Request);

        if (Response.contains("AuthorizationRequest")){
            if (Response.contains("Accepted")){
                Idle = false;
                System.out.println("AuthorizationRequest OK\n");
                AuthorizationRequest = true;
                IdleStatus();
            }
            if (Response.contains("Rejected")){
                System.out.println("AuthorizationRequest NOK");
                 AuthorizationRequest = false;
                 IdleStatus();
            }
        }
    }
    
    public static String TransactionEvent(String Action, String Reason) throws InterruptedException{
        MessageId = getDateTime();
        
        String Request = "[{2,\""+MessageId+"\",\"TransactionEvent\",{\"eventType\": \""+Action+"\", \"triggerReason\":\""+Reason+"\", \"timestamp\":\""+getTimestamp()+"\"}]\n";
        String Response = Communication(Request);

        return Response;
        
    }
    
    private static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmssSSS"); 
	Date date = new Date(); 
	return dateFormat.format(date); 
    }
    
    private static String getTimestamp() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH.mm.ss"); 
	Date date = new Date(); 
	return dateFormat.format(date); 
    }
    
    
    private static String Communication(String ResquestMessage) throws InterruptedException{
         
        String ResponseMessage = "";
        
        clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
            public String handleMessage(String ResponseMessage) {
                System.out.println(ResponseMessage);
                LogTextArea.append("Response: "+ResponseMessage+"\n");
                DisplayControls.returnMessage = ResponseMessage;
                
                return ResponseMessage;
            }
        });
        
       
        
        clientEndPoint.sendMessage(ResquestMessage);
        System.out.println(ResquestMessage);
        LogTextArea.append("Resquest: "+ResquestMessage);
        
         Thread.sleep(500); 
        
        //System.out.println("RETURN MESSAGE: "+returnMessage+"\n");
        
        ResponseMessage = returnMessage;
        
        return ResponseMessage;
    } 
   
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        UserID = new javax.swing.JTextField();
        ConnectorPlugged = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        LogTextArea = new javax.swing.JTextArea();
        ChargeStatus = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        SerialNumber = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        FirmwareVersion = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        VendorName = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        Model = new javax.swing.JLabel();
        BtnSend = new javax.swing.JButton();
        ServerConnection = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("User ID:");

        UserID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UserIDActionPerformed(evt);
            }
        });

        ConnectorPlugged.setText("Conector Plugged");
        ConnectorPlugged.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConnectorPluggedActionPerformed(evt);
            }
        });

        LogTextArea.setColumns(20);
        LogTextArea.setRows(5);
        jScrollPane1.setViewportView(LogTextArea);

        ChargeStatus.setBackground(new java.awt.Color(255, 0, 0));

        javax.swing.GroupLayout ChargeStatusLayout = new javax.swing.GroupLayout(ChargeStatus);
        ChargeStatus.setLayout(ChargeStatusLayout);
        ChargeStatusLayout.setHorizontalGroup(
            ChargeStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 98, Short.MAX_VALUE)
        );
        ChargeStatusLayout.setVerticalGroup(
            ChargeStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        jLabel2.setText("SerialNumber: ");

        SerialNumber.setText("24041991");

        jLabel4.setText("FirmwareVersion:");

        FirmwareVersion.setText("GF-001");

        jLabel6.setText("StationID:");

        DateFormat dateFormat = new SimpleDateFormat("SSS");
        Date date = new Date();
        jLabel7.setText(dateFormat.format(date));

        jLabel8.setText("VendorName:");

        VendorName.setText("ChargePoint");

        jLabel3.setText("Model:");

        Model.setText("ES01");

        BtnSend.setText("Send");
        BtnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSendActionPerformed(evt);
            }
        });

        ServerConnection.setBackground(new java.awt.Color(255, 0, 0));

        javax.swing.GroupLayout ServerConnectionLayout = new javax.swing.GroupLayout(ServerConnection);
        ServerConnection.setLayout(ServerConnectionLayout);
        ServerConnectionLayout.setHorizontalGroup(
            ServerConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        ServerConnectionLayout.setVerticalGroup(
            ServerConnectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addComponent(jScrollPane2)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SerialNumber)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(VendorName)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Model)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(FirmwareVersion)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel7))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(UserID, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(BtnSend)
                                .addGap(14, 14, 14)
                                .addComponent(ConnectorPlugged)
                                .addGap(18, 18, 18)
                                .addComponent(ServerConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(ChargeStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 61, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(UserID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(ConnectorPlugged)
                        .addComponent(BtnSend))
                    .addComponent(ServerConnection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ChargeStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(SerialNumber)
                    .addComponent(jLabel4)
                    .addComponent(FirmwareVersion)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(VendorName)
                    .addComponent(jLabel3)
                    .addComponent(Model)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void UserIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UserIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_UserIDActionPerformed

    private void BtnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSendActionPerformed
        try {
            AuthorizationRequest();
        } catch (InterruptedException ex) {
            Logger.getLogger(DisplayControls.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_BtnSendActionPerformed

    private void ConnectorPluggedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConnectorPluggedActionPerformed
        
        if (ConnectorPlugged.isSelected()){
            String Action = "Started";
            String Reason = "CabbleConnected";
            try {
                String Response = TransactionEvent(Action, Reason);
                if (Response.contains("TransactionEvent")){
                    if (Response.contains("Accepted")){
                        CabbleConnected = true;
                        System.out.println("Reservado OK\n");
                        IdleStatus();
                    }
                    else{
                        System.out.println("Reservado NOK");
                        CabbleConnected = false;
                         //Thread.sleep(5000);
                         IdleStatus();
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(DisplayControls.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            String Action = "Ended";
            String Reason = "CabbleDisonnected";
            try {
                String Response = TransactionEvent(Action, Reason);
                if (Response.contains("TransactionEvent")){
                    if (Response.contains("Accepted")){
                        CabbleConnected = false;
                        AuthorizationRequest = false;
                        System.out.println("Finalizado OK\n");
                        IdleStatus();
                    }
                    else{
                        System.out.println("Finalizado NOK");
                        CabbleConnected = true;
                         //Thread.sleep(5000);
                         IdleStatus();
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(DisplayControls.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }//GEN-LAST:event_ConnectorPluggedActionPerformed

    /**
     * @param args the command line arguments
     */

    public static boolean CabbleConnected = false;
    public static boolean AuthorizationRequest = false; 
    public static ValidateJSON ValidateJSON = new ValidateJSON();
    public static boolean Idle = false;
    public static String returnMessage = "";
    private static WebsocketClientEndpoint clientEndPoint;
    public static String MessageId;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BtnSend;
    public static javax.swing.JPanel ChargeStatus;
    public static javax.swing.JRadioButton ConnectorPlugged;
    public static javax.swing.JLabel FirmwareVersion;
    public static javax.swing.JTextArea LogTextArea;
    public static javax.swing.JLabel Model;
    public static javax.swing.JLabel SerialNumber;
    public static javax.swing.JPanel ServerConnection;
    public static javax.swing.JTextField UserID;
    public static javax.swing.JLabel VendorName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    public static javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    public javax.swing.JTextArea jTextArea2;
    // End of variables declaration//GEN-END:variables
}
