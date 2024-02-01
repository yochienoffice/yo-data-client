package com.hotsaucepos.onlineorder.dataclient;

import com.hotsaucepos.onlineorder.dataclient.model.Data;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

import com.hotsaucepos.onlineorder.dataclient.util.MongoUtil;
import org.jdatepicker.impl.DateComponentFormatter;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

public class DataClient {
    static MongoUtil mongoUtil = new MongoUtil();
    private JPanel MainPanel;
    public static JCheckBox checkBoxConvenienceFeeOnly = new JCheckBox("Convenience Fee Only");
    public JPanel dataPanel = new JPanel();
    public JScrollPane dataScrollPanel = new JScrollPane();
    public JDatePickerImpl startDatePicker;
    public JDatePickerImpl endDatePicker;
    public static JTable dataTable = new JTable();
    public static List<Data> dataSet = new ArrayList<>();

    public static final String header[] = new String[] {
            "Store ID",
            "Store Name",
            "Number of Order",
            "Convenience Fee Total",
            "Order Total"
    };

    public DataClient() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("Online Order Tool");
                frame.add(new MenuPane(frame));
                frame.pack();
                frame.setSize(800, 700);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                frame.setResizable(false);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setPreferredSize(new Dimension(600, 400));
            }
        });
    }

    public static void main(String[] args) {
        mongoUtil.createConnection();
        updateTableData();
        new DataClient();
    }

    public static void updateTableData() {
        DefaultTableModel defaultTableModel = (DefaultTableModel) dataTable.getModel();
        defaultTableModel.setRowCount(0);

        dataSet.sort(new Comparator<Data>() {
            @Override
            public int compare(Data o1, Data o2) {
                return (int) (o1.getStoreId() - o2.getStoreId());
            }
        });
        Object[] row = new Object[5];
        for (Data data : dataSet) {
            row[0] = data.getStoreId();
            row[1] = data.getStoreName();
            row[2] = data.getNumOfOrder();
            row[3] = data.getConvenienceFeeTotal();
            row[4] = data.getOrderTotal();
            defaultTableModel.addRow(new Object[]{row[0], row[1], row[2], row[3], row[4]});
        }
    }

    public class MenuPane extends JPanel {

        public MenuPane(JFrame frame) {
            setPreferredSize(new Dimension(600, 400));
            setBorder(new EmptyBorder(10, 10, 10, 10));
            setLayout(new GridBagLayout());
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
            gridBagConstraints.anchor = GridBagConstraints.CENTER;


            JComboBox comboBox = new JComboBox();

            for (Long storeId: mongoUtil.storeList) {
                comboBox.addItem(storeId);
            }


//            add(new JLabel("<html><h1><strong><i>Online Order Accounting</i></strong></h1><hr></html>"), gridBagConstraints);

            gridBagConstraints.anchor = GridBagConstraints.CENTER;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new Insets(2, 0, 2, 0);

            // Define panel for start date
            JLabel labelStartDate = new JLabel();
            labelStartDate.setText("Start Date: ");
            UtilDateModel startDateModel = new UtilDateModel();
            JDatePanelImpl startDatePanel = new JDatePanelImpl(startDateModel, new Properties());
            startDatePicker = new JDatePickerImpl(startDatePanel, new DateComponentFormatter());

            // Define panel for end date
            JLabel labelEndDate = new JLabel();
            labelEndDate.setText("End Date: ");
            UtilDateModel endDateModel = new UtilDateModel();
            JDatePanelImpl endDatePanel = new JDatePanelImpl(endDateModel, new Properties());
            endDatePicker = new JDatePickerImpl(endDatePanel, new DateComponentFormatter());

            // Define search button
            JButton searchButton = new JButton("Search");
            searchButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!startDatePicker.getJFormattedTextField().getText().isEmpty() && !endDatePicker.getJFormattedTextField().getText().isEmpty()) {
                        dataSet = mongoUtil.getDataFromDatabase(
                                startDatePicker.getJFormattedTextField().getText(),
                                endDatePicker.getJFormattedTextField().getText(),
                                checkBoxConvenienceFeeOnly.isSelected());
                        updateTableData();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Please select the valid date.");
                    }
                }
            });

            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!startDatePicker.getJFormattedTextField().getText().isEmpty() && !endDatePicker.getJFormattedTextField().getText().isEmpty()) {
                        dataSet = mongoUtil.getDataFromDatabase(
                                startDatePicker.getJFormattedTextField().getText(),
                                endDatePicker.getJFormattedTextField().getText(),
                                checkBoxConvenienceFeeOnly.isSelected());
                        List<Long> idList = mongoUtil.deleteDataFromDatabase(
                                startDatePicker.getJFormattedTextField().getText(),
                                endDatePicker.getJFormattedTextField().getText(),
                                Long.parseLong(comboBox.getSelectedItem().toString()));
                        String output = "";
                        for (Long id: idList) {
                            output = output.concat(id + ", ");
                        }
                        updateTableData();
                        JOptionPane.showMessageDialog(frame, "Order ID: " + output);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Please select the valid date.");
                    }
                }
            });

            //Define data table
            dataTable.setCellSelectionEnabled(false);
            DefaultTableModel defaultTableModel = new DefaultTableModel(0, 0);

            defaultTableModel.setColumnIdentifiers(header);
            dataTable.setModel(defaultTableModel);
            dataTable.getColumnModel().getColumn(0).setPreferredWidth(20);
            dataTable.getColumnModel().getColumn(1).setPreferredWidth(120);
            dataTable.getColumnModel().getColumn(2).setPreferredWidth(40);
            dataTable.getColumnModel().getColumn(3).setPreferredWidth(80);
            dataTable.getColumnModel().getColumn(4).setPreferredWidth(20);

            dataScrollPanel.setPreferredSize(getPreferredSize());
            dataScrollPanel.setViewportView(dataTable);

            dataPanel.add(dataScrollPanel);

            //Add to main panel
            JPanel components = new JPanel(new GridBagLayout());
            components.setSize(getPreferredSize());
//            components.add(checkBoxConvenienceFeeOnly, gridBagConstraints);
            components.add(comboBox, gridBagConstraints);
            components.add(labelStartDate, gridBagConstraints);
            components.add(startDatePicker, gridBagConstraints);
            components.add(labelEndDate, gridBagConstraints);
            components.add(endDatePicker, gridBagConstraints);
            components.add(searchButton, gridBagConstraints);
            components.add(deleteButton, gridBagConstraints);
            components.add(dataPanel, gridBagConstraints);

            gridBagConstraints.weighty = 1;
            add(components, gridBagConstraints);
        }

    }
}
