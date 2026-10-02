package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PenregonBesieged.class, GrizzlyBears.class, Shock.class})
class PenregonBesiegedTest extends BaseCardTest {

    @Test
    void endStepPerpetuallyShrinksOpponentCreatureWithLeastToughness() {
        Permanent leastToughness = harness.addToBattlefieldAndReturn(player2,
                creature("Least Toughness", 3, 2));
        Permanent largerToughness = harness.addToBattlefieldAndReturn(player2,
                creature("Larger Toughness", 4, 4));
        harness.addToBattlefield(player1, new PenregonBesieged());

        advanceToEndStep(player1);

        assertThat(gqs.getEffectivePower(gd, leastToughness)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, leastToughness)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, largerToughness)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, largerToughness)).isEqualTo(4);
    }

    @Test
    void endStepChoiceOnlyOffersCreaturesTiedForLeastToughness() {
        Permanent firstLeast = harness.addToBattlefieldAndReturn(player2,
                creature("First Least", 3, 2));
        Permanent secondLeast = harness.addToBattlefieldAndReturn(player2,
                creature("Second Least", 4, 2));
        Permanent largerToughness = harness.addToBattlefieldAndReturn(player2,
                creature("Larger Toughness", 5, 4));
        harness.addToBattlefield(player1, new PenregonBesieged());

        advanceToEndStep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstLeast.getId(), secondLeast.getId());
        harness.handlePermanentChosen(player1, secondLeast.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, firstLeast)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondLeast)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, largerToughness)).isEqualTo(4);
    }

    @Test
    void sacrificesWhenOpponentsControlNoCreatures() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new PenregonBesieged());
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Penregon Besieged");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, opponentCreature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Penregon Besieged");
        harness.assertInGraveyard(player1, "Penregon Besieged");
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Card creature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
