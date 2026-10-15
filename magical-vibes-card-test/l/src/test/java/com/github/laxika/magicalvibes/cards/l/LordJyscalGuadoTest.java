package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LordJyscalGuado.class, GrizzlyBears.class, TimberlandGuide.class})
class LordJyscalGuadoTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates at each end step after its controller puts a counter on a creature")
    void investigatesAfterControllerPutsCounterOnCreature() {
        harness.addToBattlefield(player1, new LordJyscalGuado());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Does not investigate when no counter was put on a creature")
    void doesNotInvestigateWithoutCounterPlacement() {
        harness.addToBattlefield(player1, new LordJyscalGuado());

        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("A counter put by another player does not satisfy the condition")
    void opponentCounterPlacementDoesNotInvestigate() {
        harness.addToBattlefield(player1, new LordJyscalGuado());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new TimberlandGuide()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToEndStepAndResolve(player2);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Putting a counter on an opponent's creature qualifies")
    void counterOnOpponentsCreatureQualifies() {
        harness.addToBattlefield(player1, new LordJyscalGuado());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TimberlandGuide());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Counter placement earlier in the turn qualifies even before Lord Jyscal enters")
    void earlierCounterPlacementQualifies() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TimberlandGuide());
        harness.setHand(player1, List.of(new TimberlandGuide(), new LordJyscalGuado()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Multiple counter placements still produce only one end-step investigation")
    void multipleCounterPlacementsInvestigateOnce() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LordJyscalGuado());
        harness.setHand(player1, List.of(new TimberlandGuide(), new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        for (int i = 0; i < 2; i++) {
            harness.castCreature(player1, 0, 0, target.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The intervening condition prevents a trigger when no counter was placed")
    void noCounterPlacementDoesNotPutAbilityOnStack() {
        harness.addToBattlefield(player1, new LordJyscalGuado());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Counter placement in the previous turn does not qualify in the next turn")
    void previousTurnCounterPlacementDoesNotQualify() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LordJyscalGuado());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        advanceToEndStepAndResolve(player1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.passUntil(player2, TurnStep.UPKEEP);
        advanceToEndStepAndResolve(player2);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The investigated Clue can be sacrificed for two mana to draw a card")
    void investigatedClueDrawsCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LordJyscalGuado());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.setLibrary(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        advanceToEndStepAndResolve(player1);
        Permanent clue = findPermanents(player1, "Clue").getFirst();
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, clueIndex, null, null);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Timberland Guide");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
