package com.github.comrada.wa.resolver.parser.html;

import static com.github.comrada.wa.TestUtils.readFile;
import static org.junit.jupiter.api.Assertions.*;

import com.github.comrada.wa.dto.TransactionDetail;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class EtherscanTableParserTest {

  @Test
  void parse() throws IOException {
    EtherscanTableParser parser = new EtherscanTableParser();
    String content = readFile(EtherscanTableParserTest.class, "nft-table-etherscan.html");
    TransactionDetail transactionDetail = parser.parse(content);

    assertEquals("0x6db81d551cc1d1dca0ebff2c4eb215ba112e8664", transactionDetail.fromWallet());
    assertEquals("https://etherscan.io/address/0x6db81d551cc1d1dca0ebff2c4eb215ba112e8664",
        transactionDetail.fromWalletUrl());
    assertEquals("0x7f268357a8c2552623316e2562d90e642bb538e5", transactionDetail.toWallet());
    assertEquals("https://etherscan.io/address/0x7f268357a8c2552623316e2562d90e642bb538e5",
        transactionDetail.toWalletUrl());
    assertNull(transactionDetail.blockchain());
    assertNull(transactionDetail.type());
    assertNull(transactionDetail.amount());
    assertNull(transactionDetail.asset());
    assertNull(transactionDetail.usdAmount());
    assertNull(transactionDetail.timestamp());
    assertNull(transactionDetail.hash());
    assertNull(transactionDetail.transactionUrl());
    assertNull(transactionDetail.fromName());
    assertNull(transactionDetail.toName());
  }
}