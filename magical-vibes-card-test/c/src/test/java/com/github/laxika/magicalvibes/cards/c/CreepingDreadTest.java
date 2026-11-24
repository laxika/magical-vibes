package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.t.TamiyoCollectorOfTales;
import com.github.laxika.magicalvibes.cards.w.WickerWitch;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CreepingDread.class, DevilthornFox.class, Forest.class, WickerWitch.class})
class CreepingDreadTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent who discards a card sharing a type loses 3 life")
    void matchingDiscardedTypeCausesLifeLoss() {
        resolveWithHands(List.of(new DevilthornFox()), List.of(new DevilthornFox()));

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Devilthorn Fox");
        harness.assertInGraveyard(player2, "Devilthorn Fox");
    }

    @Test
    @DisplayName("An opponent with a nonmatching discarded type does not lose life")
    void nonmatchingDiscardedTypeDoesNotCauseLifeLoss() {
        resolveWithHands(List.of(new DevilthornFox()), List.of(new Forest()));

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("No life is lost when the controller has no card to discard")
    void noControllerDiscardMeansNoLifeLoss() {
        harness.addToBattlefield(player1, new CreepingDread());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new DevilthornFox()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Devilthorn Fox");
    }

    @Test
    @DisplayName("Every player chooses before any chosen card is discarded")
    void discardedCardsAreNotRevealedBeforeEveryoneChooses() {
        harness.addToBattlefield(player1, new CreepingDread());
        harness.setHand(player1, List.of(new DevilthornFox()));
        harness.setHand(player2, List.of(new DevilthornFox(), new Forest()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.assertNotInGraveyard(player1, "Devilthorn Fox");

        harness.handleCardChosen(player2, 0);
        harness.assertInGraveyard(player1, "Devilthorn Fox");
        harness.assertInGraveyard(player2, "Devilthorn Fox");
        harness.assertInHand(player2, "Forest");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Sharing one of the controller's card's types is enough")
    void sharingOneOfSeveralTypesIsEnough() {
        resolveWithHands(List.of(new WickerWitch()), List.of(new DevilthornFox()));
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Sharing several card types still costs only 3 life")
    void sharingSeveralTypesStillLosesOnlyThreeLife() {
        resolveWithHands(List.of(new WickerWitch()), List.of(new WickerWitch()));
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("An opponent with no card to discard loses no life")
    void emptyOpponentHandDoesNotCauseLifeLoss() {
        harness.addToBattlefield(player1, new CreepingDread());
        harness.setHand(player1, List.of(new DevilthornFox()));
        harness.setHand(player2, List.of());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Devilthorn Fox");
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new CreepingDread());
        harness.setHand(player1, List.of(new DevilthornFox()));
        harness.setHand(player2, List.of(new DevilthornFox()));
        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Devilthorn Fox");
        harness.assertInHand(player2, "Devilthorn Fox");
    }

    @Test
    @CardUsed(TamiyoCollectorOfTales.class)
    @DisplayName("An opponent your effects can't make discard keeps the card and loses no life")
    void opponentProtectedFromDiscardDoesNotDiscard() {
        harness.addToBattlefield(player1, new CreepingDread());
        harness.addToBattlefieldAndReturn(player2, new TamiyoCollectorOfTales())
                .setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new DevilthornFox()));
        harness.setHand(player2, List.of(new DevilthornFox()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Devilthorn Fox");
        harness.assertInHand(player2, "Devilthorn Fox");
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed(LeylineOfTheVoid.class)
    @DisplayName("The card you discarded counts when a replacement exiles it instead")
    void controllersExiledDiscardStillCompares() {
        harness.addToBattlefield(player2, new LeylineOfTheVoid());

        resolveWithHands(List.of(new DevilthornFox()), List.of(new DevilthornFox()));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof DevilthornFox);
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    private void resolveWithHands(List<com.github.laxika.magicalvibes.model.Card> controllerHand,
                                  List<com.github.laxika.magicalvibes.model.Card> opponentHand) {
        harness.addToBattlefield(player1, new CreepingDread());
        harness.setHand(player1, controllerHand);
        harness.setHand(player2, opponentHand);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
    }
}
