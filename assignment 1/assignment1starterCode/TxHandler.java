import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
public class TxHandler  {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    private static ArrayList<UTXO> doublecheck = new ArrayList<>();
    public UTXOPool ledger;
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
      this.ledger  = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO (unspent transactions) is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     *     //////////////////////////////////////////////// ----------->>>>>>>> snippet
     *
     *     UTXO                                  verifySignature(PublicKey pubKey, byte[] message, byte[] signature)  utxo = new UTXO(in.prevTxHash, in.outputIndex);
            Transaction.Output output = utxoPool.getTxOutput(utxo);
                                                          verifySignature(PublicKey pubKey, byte[] message, byte[] signature)  
     //////////////////////////////////////////////////////////////////////////
     */






    public boolean isValidTx(Transaction tx) {

         //5  &&  4   &&  3   &&  1  && 2

        int index = 0 ;
        double inputsum=0;
        double outputsum=0;
        ArrayList<Transaction.Input> p1 = tx.getInputs();
        byte[] message;
        for(Transaction.Input u: p1) {
            UTXO utxo = new UTXO(u.prevTxHash, u.outputIndex);
            if(doublecheck.contains(utxo))
                return false;

            if(ledger.getTxOutput(utxo)==null)
                return false;
            Transaction.Output output = ledger.getTxOutput(utxo);
            inputsum += output.value;
            Crypto file2 = new Crypto();
            if(!(Crypto.verifySignature(output.address,tx.getRawDataToSign(index),u.signature)))
                return false;
            index++;
            doublecheck.add(utxo);
        }
        ArrayList<Transaction.Output> p2 = tx.getOutputs();
        for(Transaction.Output u: p2){
            if(u.value<0)
                return false;
            outputsum += u.value;
        }
        if(inputsum<outputsum)
            return false;
        return true;
        // IMPLEMENT THIS
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        int[] a = new int[possibleTxs.length];
        int i = 0;
        int length = 0;
        ArrayList<Transaction> temp = new ArrayList<>();
        for (Transaction tx1 : possibleTxs) {

            if (isValidTx(tx1)) {
                temp.add(tx1);
                a[i] = 1;
                length++;
            }
            else {
                a[i] = 0;
            }
            int j = 0;
            for(Transaction.Output ch2 : tx1.getOutputs()){

                UTXO nw = new UTXO(tx1.getHash(),j);
                ledger.addUTXO(nw, tx1.getOutput(j));
                j++;
            }
            i++;
        }
        Transaction[] result = new Transaction[length];
        i = 0;
        for (Transaction b : result) {
            b = new Transaction(temp.get(i));
            i++;
        }
        return result;
    }
}
