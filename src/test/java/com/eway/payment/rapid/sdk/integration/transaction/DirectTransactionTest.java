package com.eway.payment.rapid.sdk.integration.transaction;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.eway.payment.rapid.sdk.InputModelFactory;
import com.eway.payment.rapid.sdk.RapidClient;
import com.eway.payment.rapid.sdk.beans.external.Address;
import com.eway.payment.rapid.sdk.beans.external.CardDetails;
import com.eway.payment.rapid.sdk.beans.external.Customer;
import com.eway.payment.rapid.sdk.beans.external.PaymentDetails;
import com.eway.payment.rapid.sdk.beans.external.PaymentMethod;
import com.eway.payment.rapid.sdk.beans.external.ShippingDetails;
import com.eway.payment.rapid.sdk.beans.external.Transaction;
import com.eway.payment.rapid.sdk.beans.external.TransactionType;
import com.eway.payment.rapid.sdk.integration.IntegrationTest;
import com.eway.payment.rapid.sdk.output.CreateTransactionResponse;

public class DirectTransactionTest extends IntegrationTest {

    RapidClient client;
    Transaction t;

    @Before
    public void setup() {
        client = getSandboxClient();
        t = InputModelFactory.createTransaction();
        Customer c = InputModelFactory.initCustomer();
        Address a = InputModelFactory.initAddress();
        PaymentDetails p = InputModelFactory.initPaymentDetails();
        CardDetails cd = InputModelFactory.initCardDetails("12", "24");
        c.setCardDetails(cd);
        c.setAddress(a);
        t.setCustomer(c);
        t.setPaymentDetails(p);
        ShippingDetails shipping = InputModelFactory.createShippingDetail();
        t.setShippingDetails(shipping);
    }

    @Test
    public void testValidInput() {
        CreateTransactionResponse res = client.create(PaymentMethod.Direct, t);
        Assert.assertTrue(res.getTransactionStatus().isStatus());
        Assert.assertNotEquals(0, res.getTransactionStatus().getTransactionID());
    }

    @Test
    public void testMinimalValidInput() {
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setTotalAmount(1000);
        
        CardDetails cd = InputModelFactory.initCardDetails("12", "24");
        Customer customer = new Customer();
        customer.setCardDetails(cd);

        Transaction transaction = new Transaction();
        transaction.setPaymentDetails(paymentDetails);
        transaction.setCustomer(customer);
        transaction.setTransactionType(TransactionType.Purchase);

        CreateTransactionResponse res = client.create(PaymentMethod.Direct, transaction);
        
        Assert.assertTrue(res.getTransactionStatus().isStatus());
        Assert.assertNotEquals(0, res.getTransactionStatus().getTransactionID());
    }

    @Test
    public void testBlankInput() {
        Transaction tran = new Transaction();
        Customer c = new Customer();
        CardDetails cd = new CardDetails();
        c.setCardDetails(cd);
        tran.setCustomer(c);
        tran.setTransactionType(TransactionType.Purchase);
        CreateTransactionResponse res = client.create(PaymentMethod.Direct, tran);

        Assert.assertTrue(!res.getTransactionStatus().isStatus());
        Assert.assertEquals(0, res.getTransactionStatus().getTransactionID());
        Assert.assertTrue(res.getErrors().contains("V6021"));
        Assert.assertTrue(res.getErrors().contains("V6022"));
        Assert.assertTrue(res.getErrors().contains("V6101"));
        Assert.assertTrue(res.getErrors().contains("V6102"));
    }

    @Test
    public void testInvalidInput() {
        t.getCustomer().getCardDetails().setExpiryMonth("13");
        CreateTransactionResponse res = client.create(PaymentMethod.Direct, t);
        Assert.assertTrue(!res.getTransactionStatus().isStatus());
        Assert.assertEquals(0, res.getTransactionStatus().getTransactionID());
        Assert.assertTrue(res.getErrors().contains("V6101"));
    }

    @Test
    public void testParterIdInResponse() {
        String someParterId = "someId";
        t.setPartnerID(someParterId);
        CreateTransactionResponse res = client.create(PaymentMethod.Direct, t);
        Assert.assertTrue(res.getTransactionStatus().isStatus());
        Assert.assertEquals(someParterId, res.getTransaction().getPartnerID());
    }

    @Test
    public void testShippingDetailsInResponse() {
        CreateTransactionResponse res = client.create(PaymentMethod.Direct, t);
        Assert.assertTrue(res.getTransactionStatus().isStatus());
        Assert.assertNotNull(res.getTransaction().getShippingDetails());
        Assert.assertEquals(t.getShippingDetails(), res.getTransaction().getShippingDetails());
    }

    @Test
    public void testLineItemsInResponse() {
        CreateTransactionResponse res = client.create(PaymentMethod.Direct, t);
        Assert.assertTrue(res.getTransactionStatus().isStatus());
        Assert.assertNotNull(res.getTransaction().getLineItems());
        Assert.assertEquals(t.getLineItems(), res.getTransaction().getLineItems());
    }

    @After
    public void tearDown() {

    }

}
