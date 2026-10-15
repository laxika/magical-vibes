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

        assertThat(mine.getEffectivePower()).isEqualTo(3);
        assertThat(mine.getEffectiveToughness()).isEqualTo(3);
        assertThat(theirs.getEffectivePower()).isEqualTo(1);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(1);
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
        assertThat(mine.getEffectivePower()).isEqualTo(3);
        assertThat(theirs.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mine.getEffectivePower()).isEqualTo(2);
        assertThat(mine.getEffectiveToughness()).isEqualTo(2);
        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Your 1/1 survives while all opposing 1/1 creatures die")
    void affectsEveryCreatureOnBothSides() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());

        castPersecution();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(mine);
        assertThat(mine.getEffectivePower()).isEqualTo(2);
        assertThat(mine.getEffectiveToughness()).isEqualTo(2);
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

        assertThat(mine.getEffectivePower()).isEqualTo(1);
        assertThat(mine.getEffectiveToughness()).isEqualTo(1);
        assertThat(theirs.getEffectivePower()).isEqualTo(1);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(theirs);
    }

    @Test
    @DisplayName("Repeated casts accumulate and kill opposing 2/2 creatures")
    void repeatedCastsAccumulate() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castPersecution();
        castPersecution();

        assertThat(mine.getEffectivePower()).isEqualTo(4);
        assertThat(mine.getEffectiveToughness()).isEqualTo(4);
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

        assertThat(activePlayersCreature.getEffectivePower()).isEqualTo(1);
        assertThat(activePlayersCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(castersCreature.getEffectivePower()).isEqualTo(3);
        assertThat(castersCreature.getEffectiveToughness()).isEqualTo(3);
    }
}
