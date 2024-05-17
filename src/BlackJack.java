import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class BlackJack {
    
    private class Card {
        String value;
        String type;
        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public String toString() {
            return value + "-" + type;
        }

        public int getValue() {
            if ("AJQK".contains(value)) {
                if (value.equals("A")) {
                    return 11;
                }
                return 10;
            }
            return Integer.parseInt(value);
        }

        public boolean isAce() {
            return value.equals("A");
        }

        public String getImagePath() {
            return "./cards/" + toString() + ".png";
        }
    }

    // Variables for the game
    ArrayList<Card> deck;
    Random random = new Random();
    Card hiddenCard;
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAceCount;
    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount;
    int boardWidth = 700;
    int boardHeight = boardWidth;
    int cardWidth = 130;
    int cardHeight = 170;
    JFrame frame = new JFrame("Black Jack");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {
                // Draw hidden card
                Image hiddenCardImg = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
                if (!stayButton.isEnabled()) {
                    hiddenCardImg = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
                }
                g.drawImage(hiddenCardImg, 20, 20, cardWidth, cardHeight, null);

                // Draw dealer's hand
                for (int i = 0; i < dealerHand.size(); i++) {
                    Card card = dealerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg, cardWidth + 25 + (cardWidth + 5)*i, 20, cardWidth, cardHeight, null);
                }

                // Draw player's hand
                for (int i = 0; i < playerHand.size(); i++) {
                    Card card = playerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg, 20 + (cardWidth + 5)*i, 320, cardWidth, cardHeight, null);
                }

                if (!stayButton.isEnabled()) {
                    dealerSum = reduceDealerAce();
                    playerSum = reducePlayerAce();

                    String message = "";
                    if (playerSum > 21) {
                        message = "You lost! Try Again!";
                    }
                    else if (dealerSum > 21) {
                        message = "You won!";
                        playerMoney += bet * 2;
                    }
                    else if (playerSum == dealerSum) {
                        message = "It's a tie!";
                        playerMoney += bet; 
                    }
                    else if (playerSum > dealerSum) {
                        message = "You won!";
                        playerMoney += bet * 2; 
                    }
                    else if (playerSum < dealerSum) {
                        message = "You lost! Try Again!";
                    }

                    g.setFont(new Font("Arial", Font.PLAIN, 30));
                    g.setColor(Color.white);
                    g.drawString(message, 220, 250);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    JPanel buttonPanel = new JPanel();
    JButton hitButton = new JButton("Hit");
    JButton stayButton = new JButton("Stay");
    JButton splitButton = new JButton("Split");
    JButton restartButton = new JButton("Restart");                     
    JButton balanceButton = new JButton("Balance");
    JButton betButton = new JButton("Change Bet");
    JTextField betField = new JTextField("50", 5); 
    
    int playerMoney = 1000;
    JLabel balanceLabel = new JLabel("Balance: $" + playerMoney);
    int bet = 50; 

    BlackJack() {
        startGame();

        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setLocation(300, 50);
        // We can add resizable true to change window size but it depends on you
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(53, 101, 77));
        frame.add(gamePanel);

        // After setting focusable true player able to hit card till when player want to stop
        hitButton.setFocusable(true);
        buttonPanel.add(hitButton);
        stayButton.setFocusable(false);
        buttonPanel.add(stayButton);
        splitButton.setFocusable(false);
        buttonPanel.add(splitButton);
        restartButton.setFocusable(false); 
        buttonPanel.add(restartButton); 
        balanceButton.setFocusable(false);
        buttonPanel.add(balanceButton);
        betButton.setFocusable(false);
        buttonPanel.add(betButton);
        buttonPanel.add(new JLabel("Bet:"));
        buttonPanel.add(betField);
        buttonPanel.add(balanceLabel);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        
        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!deck.isEmpty()) {
                    Card card = deck.remove(deck.size()-1);
                    playerSum += card.getValue();
                    playerAceCount += card.isAce() ? 1 : 0;
                    playerHand.add(card);
                    if (reducePlayerAce() > 21) {
                        hitButton.setEnabled(false); 
                    }
                    gamePanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(frame, "No more cards in the deck!");
                }
            }
        });

        stayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hitButton.setEnabled(false);
                stayButton.setEnabled(false);

                while (dealerSum < 17 && !deck.isEmpty()) {
                    Card card = deck.remove(deck.size()-1);
                    dealerSum += card.getValue();
                    dealerAceCount += card.isAce() ? 1 : 0;
                    dealerHand.add(card);
                }
                gamePanel.repaint();
                
                // Update balance based on game outcome
                if (playerSum < dealerSum) {  
                    playerMoney -= bet;
                } else if (playerSum == dealerSum) {
                    playerMoney += bet; 
                } else if (playerSum > dealerSum) {
                    playerMoney += bet * 2; 
                }
                updateBalanceLabel();
            }
        });

        splitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (playerHand.size() == 2 && playerHand.get(0).value.equals(playerHand.get(1).value)) {
                    splitHand();
                    gamePanel.repaint();  
                } else {
                    JOptionPane.showMessageDialog(frame, "Split is only allowed for cards with the same value.");
                }
            }
        });
        
        restartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                restartGame();
                gamePanel.repaint();
                
            }
        });
        
        balanceButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                
                JOptionPane.showMessageDialog(frame, "Your balance: $" + playerMoney);
            }
        });
        
        betButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int newBet = Integer.parseInt(betField.getText());
                    if (newBet > 0 && newBet <= playerMoney) {
                        bet = newBet;
                        JOptionPane.showMessageDialog(frame, "Bet changed to: $" + bet);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid bet amount!");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid bet amount!");
                }
            }
        });
        
        gamePanel.repaint();
    }

    private void splitHand() {
        if (playerMoney >= bet) {
            playerMoney -= bet;
            ArrayList<Card> newHand1 = new ArrayList<>();
            ArrayList<Card> newHand2 = new ArrayList<>();
            newHand1.add(playerHand.get(0));
            newHand2.add(playerHand.get(1));
            newHand1.add(deck.remove(deck.size()-1));
            newHand2.add(deck.remove(deck.size()-1));
            playerHand = newHand1;
            gamePanel.repaint();
            startGame();
        } else {
            JOptionPane.showMessageDialog(frame, "Not enough balance for split!");
        }
    }

    public void startGame() {
        buildDeck();
        shuffleDeck();

        dealerHand = new ArrayList<Card>();
        dealerSum = 0;
        dealerAceCount = 0;

        hiddenCard = deck.remove(deck.size()-1);
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;

        Card card = deck.remove(deck.size()-1);
        dealerSum += card.getValue();
        dealerAceCount += card.isAce() ? 1 : 0;
        dealerHand.add(card);

        playerHand = new ArrayList<Card>();
        playerSum = 0;
        playerAceCount = 0;

        for (int i = 0; i < 2; i++) {
            card = deck.remove(deck.size()-1);
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
            playerHand.add(card);
        }
        
        playerMoney -= bet; 
        updateBalanceLabel();
    }

    private void updateBalanceLabel() {
        balanceLabel.setText("Balance: $" + playerMoney);
    }

    public void buildDeck() {
        deck = new ArrayList<Card>();
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H", "S"};

        for (int i = 0; i < types.length; i++) {
            for (int j = 0; j < values.length; j++) {
                Card card = new Card(values[j], types[i]);
                deck.add(card);
            }
        }
    }

    public void shuffleDeck() {
        for (int i = 0; i < deck.size(); i++) {
            int j = random.nextInt(deck.size());
            Card currCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i, randomCard);
            deck.set(j, currCard);
        }
    }

    public int reducePlayerAce() {
        while (playerSum > 21 && playerAceCount > 0) {
            playerSum -= 10;
            playerAceCount -= 1;
        }
        return playerSum;
    }

    public int reduceDealerAce() {
        while (dealerSum > 21 && dealerAceCount > 0) {
            dealerSum -= 10;
            dealerAceCount -= 1;
        }
        return dealerSum;
    }
    
    public void restartGame() {
        startGame();
        hitButton.setEnabled(true);
        stayButton.setEnabled(true);
    }

    public static void main(String[] args) {
        new BlackJack();
    }
}
