package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AkkiRockspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger adds one red mana to controller's mana pool")
    void etbAddsOneRedMana() {
        castAkkiRockspeaker();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        harness.assertOnBattlefield(player1, "Akki Rockspeaker");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(0);
        assertThat(gd.stack).isEmpty();
    }

    private void castAkkiRockspeaker() {
        harness.setHand(player1, List.of(new AkkiRockspeaker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }
}
