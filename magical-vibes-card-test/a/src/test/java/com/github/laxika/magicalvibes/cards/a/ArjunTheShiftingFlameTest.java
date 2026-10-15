package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArjunTheShiftingFlame.class, Forest.class, GrizzlyBears.class, Shock.class, AlmsCollector.class})
class ArjunTheShiftingFlameTest extends BaseCardTest {

    @Test
    @DisplayName("When you cast a spell, you put your hand on the bottom and draw that many")
    void putsHandOnBottomAndDrawsThatMany() {
        harness.addToBattlefield(player1, new ArjunTheShiftingFlame());
        Card spell = new GrizzlyBears();
        Card kept = new Forest();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(spell, kept));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(kept);
    }

    @Test
    @DisplayName("The controller orders multiple cards before putting them on the bottom")
    void ordersMultipleCardsOnBottomBeforeDrawing() {
        harness.addToBattlefield(player1, new ArjunTheShiftingFlame());
        Card spell = new GrizzlyBears();
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card drawnFirst = new Forest();
        Card drawnSecond = new GrizzlyBears();
        Card belowDraws = new Forest();
        harness.setHand(player1, List.of(spell, first, second));
        harness.setLibrary(player1, List.of(drawnFirst, drawnSecond, belowDraws));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnFirst, drawnSecond);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowDraws, second, first);
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts a spell")
    void doesNotTriggerForOpponentSpell() {
        harness.addToBattlefield(player1, new ArjunTheShiftingFlame());
        Card spell = new GrizzlyBears();
        Card kept = new Forest();
        Card libraryCard = new GrizzlyBears();
        harness.setHand(player2, List.of(spell, kept));
        harness.setLibrary(player2, List.of(libraryCard));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty hand draws no cards, even with an empty library")
    void emptyHandDoesNotDraw() {
        harness.addToBattlefield(player1, new ArjunTheShiftingFlame());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cards put on the bottom can be drawn from an initially empty library")
    void drawsBottomedCardFromEmptyLibrary() {
        harness.addToBattlefield(player1, new ArjunTheShiftingFlame());
        Card kept = new Forest();
        harness.setHand(player1, List.of(new GrizzlyBears(), kept));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A short library draws bottomed cards in the chosen order")
    void drawsOrderedBottomedCardsFromShortLibrary() {
        harness.addToBattlefield(player1, new ArjunTheShiftingFlame());
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card top = new Forest();
        harness.setHand(player1, List.of(new GrizzlyBears(), first, second));
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An instant in response triggers again and each trigger uses the current hand")
    void responseTriggersAgainAndUsesHandAtResolution() {
        harness.addToBattlefield(player1, new ArjunTheShiftingFlame());
        Card bottomed = new Forest();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new Forest();
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock(), bottomed));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(4);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondDraw, bottomed);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(3);

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomed, firstDraw);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting Arjun does not trigger its own battlefield ability")
    void doesNotTriggerForItsOwnCasting() {
        Card kept = new Forest();
        Card libraryCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new ArjunTheShiftingFlame(), kept));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Arjun, the Shifting Flame");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Alms Collector replaces Arjun's multi-card draw with one card for each player")
    void almsCollectorReplacesMultiCardDraw() {
        harness.addToBattlefield(player1, new ArjunTheShiftingFlame());
        harness.addToBattlefield(player2, new AlmsCollector());
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card drawn = new Forest();
        Card undrawn = new GrizzlyBears();
        Card opponentDraw = new Forest();
        harness.setHand(player1, List.of(new GrizzlyBears(), first, second));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawn, undrawn));
        harness.setLibrary(player2, List.of(opponentDraw));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn, first, second);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
