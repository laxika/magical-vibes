package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BrackishBlunder;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScytheclawRaptor.class, ReachThroughMists.class, BrackishBlunder.class})
class ScytheclawRaptorTest extends BaseCardTest {

    @Test
    void damagesAPlayerWhoCastsDuringAnotherPlayersTurn() {
        harness.addToBattlefield(player1, new ScytheclawRaptor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    void doesNotDamageTheActivePlayerForCastingDuringTheirTurn() {
        harness.addToBattlefield(player1, new ScytheclawRaptor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void damagesAnOpponentCastingDuringTheRaptorControllersTurn() {
        harness.addToBattlefield(player1, new ScytheclawRaptor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ReachThroughMists()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        int casterLife = gd.playerLifeTotals.get(player2.getId());
        int controllerLife = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player2, 0);

        harness.assertLife(player2, casterLife - 4);
        harness.assertLife(player1, controllerLife);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotDamageAnOpponentCastingDuringTheirOwnTurn() {
        harness.addToBattlefield(player1, new ScytheclawRaptor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ReachThroughMists()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore);
    }

    @Test
    void eachRaptorDamagesTheOffTurnCaster() {
        harness.addToBattlefield(player1, new ScytheclawRaptor());
        harness.addToBattlefield(player2, new ScytheclawRaptor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ReachThroughMists()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        int casterLife = gd.playerLifeTotals.get(player2.getId());
        int otherLife = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player2, 0);
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, casterLife - 8);
        harness.assertLife(player1, otherLife);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void triggerStillDealsDamageAfterTheRaptorReturnsToHand() {
        harness.addToBattlefield(player1, new ScytheclawRaptor());
        var raptor = findPermanent(player1, "Scytheclaw Raptor");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ReachThroughMists()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new BrackishBlunder()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        int casterLife = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player2, 0);
        assertThat(gd.stack).hasSize(2);
        harness.castAndResolveInstant(player1, 0, raptor.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(raptor);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof ScytheclawRaptor);
        harness.assertLife(player2, casterLife);
        harness.passBothPriorities();

        harness.assertLife(player2, casterLife - 4);
        assertThat(gd.stack).hasSize(1);
    }
}
