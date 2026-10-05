package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EvershrikesGift;
import com.github.laxika.magicalvibes.cards.d.DoseOfDawnglow;
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

@CardUsed({LastingTarfire.class, GrizzlyBears.class, TimberlandGuide.class,
        EvershrikesGift.class, DoseOfDawnglow.class})
class LastingTarfireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each opponent at end step after its controller puts a counter on a creature")
    void dealsDamageAfterControllerPutsCounterOnCreature() {
        harness.addToBattlefield(player1, new LastingTarfire());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Does not trigger at end step when its controller did not put a counter on a creature")
    void doesNotDealDamageWithoutCounterPlacement() {
        harness.addToBattlefield(player1, new LastingTarfire());
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A counter put by another player does not satisfy the condition")
    void opponentCounterPlacementDoesNotTrigger() {
        harness.addToBattlefield(player1, new LastingTarfire());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new TimberlandGuide()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToEndStep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Counters on an opponent's creature count and multiple placements still deal only 2 damage")
    void multiplePlacementsOnOpponentsCreatureDealDamageOnce() {
        harness.addToBattlefield(player1, new LastingTarfire());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimberlandGuide(), new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        for (int i = 0; i < 2; i++) {
            harness.castCreature(player1, 0, 0, bears.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A counter placed before Lasting Tarfire enters still satisfies its condition")
    void countsPlacementBeforeEnchantmentEnters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimberlandGuide(), new LastingTarfire()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Counter placement history is cleared for the next turn")
    void doesNotCountPlacementsFromPreviousTurn() {
        harness.addToBattlefield(player1, new LastingTarfire());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Blight counters count even if the creature dies before the end step")
    void minusCountersPaidAsCostCountAfterCreatureDies() {
        harness.addToBattlefield(player1, new LastingTarfire());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new EvershrikesGift()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player2, 20);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Triggers during an opponent's end step after its controller blights that turn")
    void triggersOnOpponentsTurn() {
        harness.addToBattlefield(player1, new LastingTarfire());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new DoseOfDawnglow()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        advanceToEndStep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Placing the first counter after the end step begins does not trigger Lasting Tarfire")
    void firstPlacementDuringEndStepDoesNotTrigger() {
        harness.addToBattlefield(player1, new LastingTarfire());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new DoseOfDawnglow()));
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.passUntilWithNoAttackers(activePlayer, TurnStep.END_STEP);
    }
}
