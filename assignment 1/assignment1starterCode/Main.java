/*
 * Main test code for Cousera cryptocurrency assignment1
 * Based on code by Sven Mentl and Pietro Brunetti
 * 
 * Copyright:
 * - Sven Mentl
 * - Pietro Brunetti
 * - Bruce Arden
 * - Tero Keski-Valkama
 */

import java.math.BigInteger;
import java.security.*;

public class Main {

   public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        /*
         * Generate key pairs, for Scrooge & Alice
         */
        KeyPair pk_scrooge = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        KeyPair pk_alice   = KeyPairGenerator.getInstance("RSA").generateKeyPair();
       KeyPair pk_bob   = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        /*
         * Set up the root transaction:
         *
         * Generating a root transaction tx out of thin air, so that Scrooge owns a coin of value 10
         * By thin air I mean that this tx will not be validated, I just need it to get
         * a proper Transaction.Output which I then can put in the UTXOPool, which will be passed
         * to the TXHandler.
         */
        Tx tx = new Tx();
        tx.addOutput(10, pk_scrooge.getPublic());

        // This value has no meaning, but tx.getRawDataToSign(0) will access it in prevTxHash;
        byte[] initialHash = BigInteger.valueOf(0).toByteArray();
        tx.addInput(initialHash, 0);

        tx.signTx(pk_scrooge.getPrivate(), 0);

        /*
         * Set up the UTXOPool
         */
        // The transaction output of the root transaction is the initial unspent output.
        UTXOPool utxoPool = new UTXOPool();
        UTXO utxo = new UTXO(tx.getHash(),0);
        utxoPool.addUTXO(utxo, tx.getOutput(0));

        /*  
         * Set up a test Transaction
         */
       Tx tx2 = new Tx();
       tx2.addInput(tx.getHash(), 0);
       tx2.addOutput(5, pk_alice.getPublic());
       tx2.addOutput(3, pk_alice.getPublic());
       tx2.addOutput(2, pk_alice.getPublic());
       tx2.signTx(pk_scrooge.getPrivate(), 0);

       Tx tx3 = new Tx();
       tx3.addInput(tx2.getHash(), 0);
       tx3.addInput(tx2.getHash(), 1);

       tx3.addOutput(7, pk_bob.getPublic());
       tx3.addOutput(1, pk_alice.getPublic());

       tx3.signTx(pk_alice.getPrivate(), 0);
       tx3.signTx(pk_alice.getPrivate(), 1);

       Tx tx4 = new Tx();
       tx4.addInput(tx2.getHash(), 0);
       tx4.addInput(tx2.getHash(), 1);

       tx4.addOutput(4, pk_bob.getPublic());
       tx4.addOutput(4, pk_alice.getPublic());

       tx4.signTx(pk_alice.getPrivate(), 0);
       tx4.signTx(pk_alice.getPrivate(), 1);

       Tx tx5 = new Tx();
       tx5.addInput(tx3.getHash(), 0);

       tx5.addOutput(3, pk_bob.getPublic());
       tx5.addOutput(4, pk_alice.getPublic());
       tx5.signTx(pk_bob.getPrivate(), 0);

       Tx tx6 = new Tx();
       tx6.addInput(tx2.getHash(), 2);
       tx6.addOutput(2, pk_bob.getPublic());
       tx6.signTx(pk_alice.getPrivate(), 0);

       Tx tx7 = new Tx();
       tx7.addInput(tx3.getHash(), 1);
       tx7.addOutput(1, pk_bob.getPublic());
       tx7.signTx(pk_alice.getPrivate(), 0);

       Tx tx8 = new Tx();
       tx8.addInput(tx5.getHash(), 1);
       tx8.addOutput(4, pk_bob.getPublic());
       tx8.signTx(pk_alice.getPrivate(), 0);

        /*
         * Start the test
         */
        // Remember that the utxoPool contains a single unspent Transaction.Output which is
        // the coin from Scrooge.
       TxHandler txHandler = new TxHandler(utxoPool);
 /*      System.out.println("txHandler.isValidTx(tx2) returns: " + txHandler.isValidTx(tx2));
       System.out.println("txHandler.isValidTx(tx3) returns: " + txHandler.isValidTx(tx3));
       System.out.println("txHandler.isValidTx(tx4) returns: " + txHandler.isValidTx(tx4));
       System.out.println("txHandler.isValidTx(tx5) returns: " + txHandler.isValidTx(tx5));
       System.out.println("txHandler.isValidTx(tx6) returns: " + txHandler.isValidTx(tx6));
       System.out.println("txHandler.isValidTx(tx7) returns: " + txHandler.isValidTx(tx7));
       System.out.println("txHandler.isValidTx(tx8) returns: " + txHandler.isValidTx(tx8));*/
       System.out.println("txHandler.handleTxs(new Transaction[]{tx2}) returns: " +
            txHandler.handleTxs(new Transaction[]{tx2,tx3,tx5,tx6,tx7,tx8}).length + " transaction(s)");
    }


    public static class Tx extends Transaction { 
        public void signTx(PrivateKey sk, int input) throws SignatureException {
            Signature sig = null;
            try {
                sig = Signature.getInstance("SHA256withRSA");
                sig.initSign(sk);
                sig.update(this.getRawDataToSign(input));
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            this.addSignature(sig.sign(),input);
            // Note that this method is incorrectly named, and should not in fact override the Java
            // object finalize garbage collection related method.
            this.finalize();
        }
    }
}
