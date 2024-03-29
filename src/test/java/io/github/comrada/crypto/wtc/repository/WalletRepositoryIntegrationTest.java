package io.github.comrada.crypto.wtc.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.comrada.crypto.wtc.model.Wallet;
import io.github.comrada.crypto.wtc.model.WalletId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
class WalletRepositoryIntegrationTest {

  @Autowired
  WalletRepository repository;

  @Test
  @Sql("wallets.sql")
  void findAllTest() {
    List<Wallet> addresses = repository.findAll();
    assertEquals(5, addresses.size());
  }

  @Test
  @Sql("wallets.sql")
  void addWalletDoesNotChangeExistingRecord() {
    WalletId id = WalletId.builder()
        .blockchain("Ripple")
        .address("rfTjtcvf4mBLP5hpD38RjtdAFTZdr31uiY")
        .asset("XRP")
        .build();

    Optional<Wallet> item1 = repository.findById(id);
    assertTrue(item1.isPresent());
    Wallet wallet1 = item1.get();
    repository.addWallet("Ripple", "rfTjtcvf4mBLP5hpD38RjtdAFTZdr31uiY", "XRP");
    Optional<Wallet> item12 = repository.findById(id);
    assertTrue(item12.isPresent());
    Wallet wallet12 = item12.get();
    assertEquals(wallet1, wallet12);
    assertEquals(wallet1.getBalance(), wallet12.getBalance());
    assertEquals(wallet1.getCheckedAt(), wallet12.getCheckedAt());
  }

  @Test
  void addWallet() {
    WalletId id = WalletId.builder()
        .blockchain("Ripple")
        .address("rw2ciyaNshpHe7bCHo4bRWq6pqqynnWKQg")
        .asset("XRP")
        .build();
    Wallet expected = new Wallet();
    expected.setId(id);
    repository.addWallet("Ripple", "rw2ciyaNshpHe7bCHo4bRWq6pqqynnWKQg", "XRP");

    Optional<Wallet> item = repository.findById(id);
    assertTrue(item.isPresent());
    Wallet actual = item.get();
    assertEquals(expected, actual);
  }

  @Test
  void emptyAssetIsNotStored() {
    repository.addWallet("", "rw2ciyaNshpHe7bCHo4bRWq6pqqynnWKQg", "");

    List<Wallet> wallets = repository.findAll();
    assertTrue(wallets.isEmpty());
  }

  @Test
  void emptyAddressIsNotStored() {
    repository.addWallet("Ripple", "", "XRP");

    List<Wallet> wallets = repository.findAll();
    assertTrue(wallets.isEmpty());
  }

  @Test
  void addWalletWithExchange() {
    WalletId id = WalletId.builder()
        .blockchain("Ripple")
        .address("rw2ciyaNshpHe7bCHo4bRWq6pqqynnWKQg")
        .asset("XRP")
        .build();
    Wallet expected = new Wallet();
    expected.setId(id);
    repository.addWallet("Ripple", "rw2ciyaNshpHe7bCHo4bRWq6pqqynnWKQg", "XRP", true, false);

    Optional<Wallet> item = repository.findById(id);
    assertTrue(item.isPresent());
    Wallet actual = item.get();
    assertEquals(expected, actual);
    assertTrue(actual.isExchange());
  }
}