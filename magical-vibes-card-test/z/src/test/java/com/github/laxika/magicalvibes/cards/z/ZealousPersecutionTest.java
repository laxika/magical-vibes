package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZealousPersecution.class, GrizzlyBears.class, FugitiveWizard.class})
class ZealousPersecutionTest extends BaseCardTest {

    private void castPersecution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new ZealousPersecution(), "{W}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Your creatures get +1/+1 and opponents' creatures get -1/-1")
    void bothClausesApply() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());   // 2/2
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2

        castPersecution();

        assertThat(gqs.getEffectivePower(gd, mine)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mine)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, theirs)).isEqualTo(1);
    }

    @Test
    @DisplayName("-1/-1 kills an opponent's 1/1")
    void killsOpponentOneOne() {
        harness.addToBattlefield(player2, new FugitiveWizard()); // 1/1

        castPersecution();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Boosts wear off at end of turn")
    void wearsOff() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());   // 2/2
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2

        castPersecution();
        assertThat(gqs.getEffectivePower(gd, mine)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mine)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mine)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, theirs)).isEqualTo(2);
    }

    @Test
    @DisplayName("Your 1/1 survives while all opposing 1/1 creatures die")
    void affectsEveryCreatureOnBothSides() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());

        castPersecution();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(mine);
        assertThat(gqs.getEffectivePower(gd, mine)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mine)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution are unaffected")
    void doesNotAffectCreaturesEnteringLater() {
        castPersecution();

        Permanent mine = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        harness.runStateBasedActions();

        assertThat(gqs.getEffectivePower(gd, mine)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mine)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, theirs)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(theirs);
    }

    @Test
    @DisplayName("Repeated casts accumulate and kill opposing 2/2 creatures")
    void repeatedCastsAccumulate() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castPersecution();
        castPersecution();

        assertThat(gqs.getEffectivePower(gd, mine)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mine)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The spell uses its controller even during the opponent's turn")
    void opponentCanCastOnActivePlayersTurn() {
        Permanent activePlayersCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent castersCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new ZealousPersecution(), "{W}{B}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, activePlayersCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, activePlayersCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, castersCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, castersCreature)).isEqualTo(3);
    }
}
