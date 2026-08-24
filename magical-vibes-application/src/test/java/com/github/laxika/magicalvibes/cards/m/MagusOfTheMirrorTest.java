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
        beginUpkeep();

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
        beginUpkeep();

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
    @DisplayName("Does not partially exchange life totals when a player cannot gain life")
    void doesNotPartiallyExchangeWhenPlayerCannotGainLife() {
        addReadyMagus();
        harness.addToBattlefield(player2, new RampagingFerocidon());
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);
        beginUpkeep();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);
    }

    private Permanent addReadyMagus() {
        Permanent magus = harness.addToBattlefieldAndReturn(player1, new MagusOfTheMirror());
        magus.setSummoningSick(false);
        return magus;
    }

    private void beginUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }
}
