package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AkkiRockspeaker.class)
class AkkiRockspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger adds one red mana to controller's mana pool")
    void etbAddsOneRedMana() {
        harness.castFromHand(player1, new AkkiRockspeaker(), "{1}{R}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Akki Rockspeaker");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(0);
        assertThat(gd.stack).isEmpty();
    }
}
