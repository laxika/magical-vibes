package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.o.OranRiefInvoker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoilmagesTrick.class, OranRiefInvoker.class})
class RoilmagesTrickTest extends BaseCardTest {

    @Test
    @DisplayName("Gives opposing creatures -X/-0 and draws a card")
    void debuffsOpposingCreaturesAndDrawsCard() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new OranRiefInvoker());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new OranRiefInvoker());
        OranRiefInvoker drawnCard = new OranRiefInvoker();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.setHand(player1, List.of(new RoilmagesTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(-1);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Counts each colored mana only once and excludes colorless mana")
    void countsDistinctColorsOnly() {
        harness.addToBattlefield(player2, new OranRiefInvoker());

        harness.setHand(player1, List.of(new RoilmagesTrick()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanent(player2, "Oran-Rief Invoker").getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("The penalty wears off at end of turn")
    void penaltyWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new OranRiefInvoker());

        harness.setHand(player1, List.of(new RoilmagesTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        Permanent opposingCreature = findPermanent(player2, "Oran-Rief Invoker");
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(opposingCreature.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Four colors spent reduce every opposing creature's power by four")
    void fourColorsAffectEveryOpposingCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new OranRiefInvoker());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new OranRiefInvoker());
        harness.setHand(player1, List.of(new RoilmagesTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(first.getEffectivePower()).isEqualTo(-2);
        assertThat(second.getEffectivePower()).isEqualTo(-2);
        assertThat(first.getEffectiveToughness()).isEqualTo(2);
        assertThat(second.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Draws exactly one card when there are no creatures")
    void drawsWithNoCreatures() {
        OranRiefInvoker drawnCard = new OranRiefInvoker();
        OranRiefInvoker nextCard = new OranRiefInvoker();
        harness.setLibrary(player1, List.of(drawnCard, nextCard));
        harness.setHand(player1, List.of(new RoilmagesTrick()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        harness.assertInGraveyard(player1, "Roilmage's Trick");
    }

    @Test
    @DisplayName("Creatures entering after resolution are not affected")
    void creaturesEnteringLaterAreUnaffected() {
        Permanent affected = harness.addToBattlefieldAndReturn(player2, new OranRiefInvoker());
        harness.setHand(player1, List.of(new RoilmagesTrick()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0);
        Permanent later = harness.addToBattlefieldAndReturn(player2, new OranRiefInvoker());

        assertThat(affected.getEffectivePower()).isEqualTo(1);
        assertThat(later.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Selects affected creatures on resolution rather than on casting")
    void creaturesEnteringBeforeResolutionAreAffected() {
        harness.setHand(player1, List.of(new RoilmagesTrick()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new OranRiefInvoker());
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }
}
