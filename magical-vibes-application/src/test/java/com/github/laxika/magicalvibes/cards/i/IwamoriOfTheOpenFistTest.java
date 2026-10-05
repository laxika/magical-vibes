package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CrackTheEarth;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IwamoriOfTheOpenFist.class, CrackTheEarth.class, GnarledMass.class, IsaoEnlightenedBushi.class,
        GodsEyeGateToTheReikai.class})
class IwamoriOfTheOpenFistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only legendary creature cards from the opponent's hand")
    void offersOnlyLegendaryCreatures() {
        Card legendaryCreature = new IsaoEnlightenedBushi();
        resolveIwamoriWithOpponentHand(List.of(new CrackTheEarth(), new GnarledMass(), legendaryCreature));

        PendingInteraction.HandChoice choice = (PendingInteraction.HandChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIndices()).containsExactly(2);
    }

    @Test
    @DisplayName("Opponent may put the chosen legendary creature onto the battlefield")
    void opponentPutsChosenLegendaryCreatureOntoBattlefield() {
        Card legendaryCreature = new IsaoEnlightenedBushi();
        resolveIwamoriWithOpponentHand(List.of(new CrackTheEarth(), new GnarledMass(), legendaryCreature));

        harness.handleCardChosen(player2, 2);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == legendaryCreature);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
    }

    @Test
    @DisplayName("ETB does not offer a legendary noncreature card")
    void doesNotOfferLegendaryNoncreature() {
        Card legendaryLand = new GodsEyeGateToTheReikai();
        Card legendaryCreature = new IsaoEnlightenedBushi();
        resolveIwamoriWithOpponentHand(List.of(legendaryLand, legendaryCreature));

        PendingInteraction.HandChoice choice = (PendingInteraction.HandChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Opponent may decline putting a legendary creature onto the battlefield")
    void opponentMayDeclineLegendaryCreature() {
        Card legendaryCreature = new IsaoEnlightenedBushi();
        resolveIwamoriWithOpponentHand(List.of(legendaryCreature));

        harness.handleCardChosen(player2, -1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(legendaryCreature);
        harness.assertNotOnBattlefield(player2, "Isao, Enlightened Bushi");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
    }

    @Test
    @DisplayName("ETB does not offer a nonlegendary creature card")
    void doesNotOfferNonlegendaryCreature() {
        resolveIwamoriWithOpponentHand(List.of(new CrackTheEarth(), new GnarledMass()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Opponent puts only one of multiple eligible legendary creatures onto the battlefield")
    void putsOnlyOneLegendaryCreatureOntoBattlefield() {
        Card first = new IsaoEnlightenedBushi();
        Card second = new IsaoEnlightenedBushi();
        resolveIwamoriWithOpponentHand(List.of(first, second));

        PendingInteraction.HandChoice choice = (PendingInteraction.HandChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(second);
                    assertThat(permanent.isTapped()).isFalse();
                    assertThat(permanent.isSummoningSick()).isTrue();
                });
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent with an empty hand is not prompted")
    void emptyOpponentHandDoesNotPrompt() {
        resolveIwamoriWithOpponentHand(List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Iwamori of the Open Fist");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller cannot put their own legendary creature onto the battlefield")
    void doesNotOfferControllerHand() {
        harness.castFromHand(player1, new IwamoriOfTheOpenFist(), "{2}{G}{G}");
        harness.passBothPriorities();
        Card controllerCreature = new IsaoEnlightenedBushi();
        harness.setHand(player1, List.of(controllerCreature));
        harness.setHand(player2, List.of());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerCreature);
        harness.assertNotOnBattlefield(player1, "Isao, Enlightened Bushi");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A legendary creature put onto the battlefield still triggers its own ETB ability")
    void chosenIwamoriTriggersForTheOtherPlayer() {
        Card opponentIwamori = new IwamoriOfTheOpenFist();
        resolveIwamoriWithOpponentHand(List.of(opponentIwamori));
        Card controllerCreature = new IsaoEnlightenedBushi();
        harness.setHand(player1, List.of(controllerCreature));

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement().satisfies(permanent -> assertThat(permanent.getCard()).isSameAs(opponentIwamori));
        harness.passBothPriorities();

        PendingInteraction.HandChoice choice = (PendingInteraction.HandChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Isao, Enlightened Bushi");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void resolveIwamoriWithOpponentHand(List<Card> opponentHand) {
        harness.castFromHand(player1, new IwamoriOfTheOpenFist(), "{2}{G}{G}");
        harness.passBothPriorities();

        harness.setHand(player2, opponentHand);
        harness.passBothPriorities();
    }
}
