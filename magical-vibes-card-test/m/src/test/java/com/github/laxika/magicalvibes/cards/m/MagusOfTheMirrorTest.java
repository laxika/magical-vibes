package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RampagingFerocidon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagusOfTheMirror.class, RampagingFerocidon.class})
class MagusOfTheMirrorTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges life totals with a target opponent and sacrifices itself")
    void exchangesLifeTotalsWithTargetOpponent() {
        addReadyMagus();
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 5);
        harness.assertNotOnBattlefield(player1, "Magus of the Mirror");
    }

    @Test
    @DisplayName("Can only target an opponent")
    void canOnlyTargetOpponent() {
        addReadyMagus();
        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Can only be activated during its controller's upkeep")
    void canOnlyBeActivatedDuringYourUpkeep() {
        addReadyMagus();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Can only be activated during its controller's upkeep")
    void cannotBeActivatedDuringOpponentsUpkeep() {
        addReadyMagus();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your upkeep");
    }

    @Test
    @DisplayName("Cannot be activated while tapped")
    void cannotBeActivatedWhileTapped() {
        Permanent magus = addReadyMagus();
        advanceToUpkeep(player1);
        magus.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Does not partially exchange life totals when a player cannot gain life")
    void doesNotPartiallyExchangeWhenPlayerCannotGainLife() {
        addReadyMagus();
        harness.addToBattlefield(player2, new RampagingFerocidon());
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Magus of the Mirror");
    }

    private Permanent addReadyMagus() {
        Permanent magus = harness.addToBattlefieldAndReturn(player1, new MagusOfTheMirror());
        magus.setSummoningSick(false);
        return magus;
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and the exchange uses life totals at resolution")
    void sacrificesBeforeResolutionAndUsesCurrentLifeTotals() {
        addReadyMagus();
        advanceToUpkeep(player1);
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Magus of the Mirror");
        harness.assertInGraveyard(player1, "Magus of the Mirror");
        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);
        harness.setLife(player1, 8);
        harness.setLife(player2, 17);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("The controller can lose life while the opponent gains life")
    void exchangesWhenControllerHasMoreLife() {
        addReadyMagus();
        advanceToUpkeep(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Equal life totals remain unchanged and the sacrifice is still paid")
    void exchangesEqualLifeTotals() {
        addReadyMagus();
        advanceToUpkeep(player1);
        harness.setLife(player1, 12);
        harness.setLife(player2, 12);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 12);
        harness.assertInGraveyard(player1, "Magus of the Mirror");
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWithSummoningSickness() {
        advanceToUpkeep(player1);
        Permanent magus = harness.addToBattlefieldAndReturn(player1, new MagusOfTheMirror());
        magus.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, "Magus of the Mirror");
        harness.assertNotInGraveyard(player1, "Magus of the Mirror");
    }

}
