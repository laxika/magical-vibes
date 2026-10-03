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

    @Test
    @DisplayName("Entering puts the mana trigger on the stack before adding mana")
    void manaIsAddedOnlyWhenTriggerResolves() {
        harness.castFromHand(player1, new AkkiRockspeaker(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Akki Rockspeaker");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast adds mana to the entering creature's controller")
    void enteringWithoutCastingAddsManaToController() {
        harness.enterBattlefieldAndReturn(player2, new AkkiRockspeaker());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();

        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Akki Rockspeaker");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
