package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WornPowerstone.class})
class WornPowerstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Worn Powerstone enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new WornPowerstone(), "{3}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Worn Powerstone").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping Worn Powerstone adds two colorless mana")
    void tappingAddsTwoColorlessMana() {
        Permanent powerstone = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        GameData gameData = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);

        assertThat(powerstone.isTapped()).isTrue();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gameData.stack).isEmpty();
    }

    @Test
    @DisplayName("Worn Powerstone enters tapped even when not cast")
    void entersTappedWithoutBeingCast() {
        Permanent powerstone = harness.enterBattlefieldAndReturn(player1, new WornPowerstone());

        assertThat(powerstone.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Worn Powerstone cannot produce mana while tapped")
    void cannotActivateWhileTapped() {
        Permanent powerstone = harness.enterBattlefieldAndReturn(player1, new WornPowerstone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(powerstone.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Worn Powerstone can produce mana after its controller untaps it")
    void producesManaAfterUntapping() {
        Permanent powerstone = harness.enterBattlefieldAndReturn(player2, new WornPowerstone());
        harness.performUntapStep(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThat(powerstone.isTapped()).isFalse();
        harness.activateAbility(player2, 0, null, null);

        assertThat(powerstone.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }
}
