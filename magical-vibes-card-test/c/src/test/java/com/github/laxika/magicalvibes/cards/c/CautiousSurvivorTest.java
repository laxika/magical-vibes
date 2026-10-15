package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RelentlessAssault;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CautiousSurvivor.class, RelentlessAssault.class})
class CautiousSurvivorTest extends BaseCardTest {

    @Test
    void tappedSurvivorGainsTwoLifeAtPostcombatMain() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new CautiousSurvivor());
        survivor.tap();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void untappedSurvivorDoesNotTrigger() {
        harness.addToBattlefield(player1, new CautiousSurvivor());

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void untappingBeforeResolutionPreventsLifeGain() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new CautiousSurvivor());
        survivor.tap();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToPostcombatMain(player1);
        survivor.untap();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void doesNotTriggerDuringOpponentsSecondMainPhase() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new CautiousSurvivor());
        survivor.tap();

        advanceToPostcombatMain(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappingAfterSecondMainBeginsDoesNotTrigger() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new CautiousSurvivor());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToPostcombatMain(player1);
        survivor.tap();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void doesNotTriggerAgainDuringThirdMainPhase() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new CautiousSurvivor());
        survivor.tap();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 2);

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore + 2);
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(activePlayer, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
