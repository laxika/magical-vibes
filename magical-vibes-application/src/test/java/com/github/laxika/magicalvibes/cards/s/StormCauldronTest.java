package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StormCauldronTest extends BaseCardTest {

    // ===== Static: extra land play for every player =====

    @Test
    @DisplayName("Each player may play one additional land per turn")
    void raisesLandPlayLimitForEachPlayer() {
        harness.addToBattlefield(player1, new StormCauldron());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Storm Cauldrons grant two additional land plays")
    void twoStormCauldronsStack() {
        harness.addToBattlefield(player1, new StormCauldron());
        harness.addToBattlefield(player2, new StormCauldron());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(3);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(3);
    }

    // ===== Trigger: tapped land returns to owner's hand =====

    @Test
    @DisplayName("Tapping a land for mana returns it to its owner's hand")
    void tappingLandReturnsItToHand() {
        harness.addToBattlefield(player1, new StormCauldron());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("The mana produced by the tapped land remains in the pool")
    void manaRemainsAfterLandReturns() {
        harness.addToBattlefield(player1, new StormCauldron());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Symmetric — an opponent's tapped land also returns to their hand")
    void symmetricReturnsOpponentsTappedLand() {
        harness.addToBattlefield(player1, new StormCauldron());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Without Storm Cauldron a tapped land stays on the battlefield")
    void noReturnWithoutStormCauldron() {
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
    }
}
