package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaraudingSphinx.class, GrizzlyBears.class, Shock.class})
class MaraudingSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils two after the controller commits a crime")
    void surveilsTwoAfterCrime() {
        Card topCard = new GrizzlyBears();
        Card secondCard = new Shock();
        harness.addToBattlefield(player1, new MaraudingSphinx());
        harness.setLibrary(player1, List.of(topCard, secondCard));
        castShockAtOpponent();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        finishScry(List.of(1), List.of(0));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The crime trigger fires only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new MaraudingSphinx());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        finishScry(List.of(1), List.of(0));
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Targeting yourself does not commit a crime")
    void targetingYourselfDoesNotTrigger() {
        Card topCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new MaraudingSphinx());
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("An opponent's crime does not trigger your Sphinx")
    void opponentsCrimeDoesNotTrigger() {
        Card topCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new MaraudingSphinx());
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Targeting an opposing creature triggers surveil before the spell resolves")
    void targetingOpposingCreatureTriggers() {
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new MaraudingSphinx());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        finishScry(List.of(), List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Surveil can keep both cards in a different order")
    void canReorderBothCardsOnTop() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new Shock();
        Card thirdCard = new MaraudingSphinx();
        harness.addToBattlefield(player1, new MaraudingSphinx());
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));
        castShockAtOpponent();

        harness.passBothPriorities();
        finishScry(List.of(1, 0), List.of());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, firstCard, thirdCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(firstCard, secondCard);
    }

    @Test
    @DisplayName("Surveil two works with only one card in the library")
    void surveilsSingleRemainingCard() {
        Card topCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new MaraudingSphinx());
        harness.setLibrary(player1, List.of(topCard));
        castShockAtOpponent();

        harness.passBothPriorities();
        finishScry(List.of(), List.of(0));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Surveilling an empty library does not draw or request a choice")
    void surveilsEmptyLibrary() {
        harness.addToBattlefield(player1, new MaraudingSphinx());
        harness.setLibrary(player1, List.of());
        castShockAtOpponent();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Each Sphinx has its own once-per-turn trigger")
    void multipleSphinxesTriggerIndependently() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new Shock();
        harness.addToBattlefield(player1, new MaraudingSphinx());
        harness.addToBattlefield(player1, new MaraudingSphinx());
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        castShockAtOpponent();

        harness.passBothPriorities();
        finishScry(List.of(0, 1), List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        finishScry(List.of(), List.of(0, 1));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstCard, secondCard);
    }

    @Test
    @DisplayName("The crime trigger resets on the opponent's turn")
    void triggersAgainOnOpponentsTurn() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new Shock();
        harness.addToBattlefield(player1, new MaraudingSphinx());
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        castShockAtOpponent();
        harness.passBothPriorities();
        finishScry(List.of(0, 1), List.of());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        castShockAtOpponent();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        finishScry(List.of(), List.of(0, 1));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstCard, secondCard);
    }

    private void castShockAtOpponent() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
    }

    private void finishScry(List<Integer> topIndices, List<Integer> graveyardIndices) {
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(topIndices, graveyardIndices));
    }
}
