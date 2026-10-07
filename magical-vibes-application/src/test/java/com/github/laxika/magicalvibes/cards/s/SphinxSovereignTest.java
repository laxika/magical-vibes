package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SphinxSovereign.class)
class SphinxSovereignTest extends BaseCardTest {

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities(); // resolve trigger
    }

    @Test
    @DisplayName("Untapped at end step: controller gains 3 life, opponent unaffected")
    void untappedGainsThreeLife() {
        harness.addToBattlefield(player1, new SphinxSovereign());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveEndStepTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapped at end step: each opponent loses 3 life, controller unaffected")
    void tappedDrainsOpponents() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxSovereign());
        sphinx.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveEndStepTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void usesTappedStatusAtResolution(boolean initiallyTapped) {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxSovereign());
        if (initiallyTapped) sphinx.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        if (initiallyTapped) sphinx.untap();
        else sphinx.tap();
        harness.passBothPriorities();

        harness.assertLife(player1, initiallyTapped ? 23 : 20);
        harness.assertLife(player2, initiallyTapped ? 20 : 17);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void usesTappedStatusWhenSourceLeavesBattlefield(boolean tappedWhenLeaving) {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxSovereign());
        if (!tappedWhenLeaving) sphinx.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        if (tappedWhenLeaving) sphinx.tap();
        else sphinx.untap();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, sphinx));
        harness.assertInGraveyard(player1, "Sphinx Sovereign");
        harness.passBothPriorities();

        harness.assertLife(player1, tappedWhenLeaving ? 20 : 23);
        harness.assertLife(player2, tappedWhenLeaving ? 17 : 20);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new SphinxSovereign());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
