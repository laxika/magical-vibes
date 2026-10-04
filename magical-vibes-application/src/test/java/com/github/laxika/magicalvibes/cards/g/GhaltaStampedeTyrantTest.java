package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.r.RampagingCeratops;
import com.github.laxika.magicalvibes.cards.v.VerdantSunsAvatar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhaltaStampedeTyrant.class, RampagingCeratops.class, Abrade.class, VerdantSunsAvatar.class})
class GhaltaStampedeTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Entering puts any number of creature cards from hand onto the battlefield")
    void putsAnyNumberOfCreaturesFromHandOntoBattlefield() {
        Card ghalta = new GhaltaStampedeTyrant();
        Card ceratopsOne = new RampagingCeratops();
        Card ceratopsTwo = new RampagingCeratops();
        Card abrade = new Abrade();
        harness.setHand(player1, List.of(ghalta, ceratopsOne, ceratopsTwo, abrade));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        var choice = (PendingInteraction.HandCardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactlyInAnyOrder(0, 1);
        assertThat(choice.putAnyNumber()).isTrue();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(abrade);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof RampagingCeratops)
                .hasSize(2)
                .allMatch(p -> !p.isTapped());
    }

    @Test
    @DisplayName("Declining the choice puts no creature cards from hand onto the battlefield")
    void decliningPutsNoCreaturesFromHandOntoBattlefield() {
        Card ghalta = new GhaltaStampedeTyrant();
        Card ceratops = new RampagingCeratops();
        harness.setHand(player1, List.of(ghalta, ceratops));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ceratops);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof RampagingCeratops)
                .isEmpty();
    }

    @Test
    @DisplayName("Choosing one creature and then finishing leaves the others in hand")
    void canPutOnlySomeCreaturesOntoBattlefield() {
        Card selected = new RampagingCeratops();
        Card remaining = new RampagingCeratops();
        harness.setHand(player1, List.of(new GhaltaStampedeTyrant(), selected, remaining));
        harness.setHand(player2, List.of(new RampagingCeratops()));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() == selected).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Rampaging Ceratops");
    }

    @Test
    @DisplayName("With no creature cards in hand the trigger finishes without a choice")
    void noEligibleCardsFinishesWithoutInteraction() {
        Card abrade = new Abrade();
        harness.setHand(player1, List.of(new GhaltaStampedeTyrant(), abrade));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(abrade);
        harness.assertOnBattlefield(player1, "Ghalta, Stampede Tyrant");
    }

    @Test
    @DisplayName("Creatures entering together see each other's entry regardless of selection order")
    void creaturesEnterSimultaneously() {
        harness.setHand(player1, List.of(new GhaltaStampedeTyrant(),
                new RampagingCeratops(), new VerdantSunsAvatar()));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Rampaging Ceratops");
        harness.assertOnBattlefield(player1, "Verdant Sun's Avatar");
        harness.assertLife(player1, 29);
        harness.assertLife(player2, 20);
    }
}
