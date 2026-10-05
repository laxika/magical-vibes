package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({Mindmoil.class, BirdsOfParadise.class, Forest.class, Mountain.class})
class MindmoilTest extends BaseCardTest {

    @Test
    @DisplayName("After you cast a spell, you put your hand on the bottom of your library and draw that many")
    void replacesHandAfterCastingSpell() {
        Forest drawnCard = new Forest();
        Mountain nextLibraryCard = new Mountain();
        Mountain remainingHandCard = new Mountain();

        harness.addToBattlefield(player1, new Mindmoil());
        harness.setHand(player1, List.of(new BirdsOfParadise(), remainingHandCard));
        harness.setLibrary(player1, List.of(drawnCard, nextLibraryCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextLibraryCard, remainingHandCard);
    }

    @Test
    @DisplayName("The controller orders multiple hand cards before drawing that many")
    void ordersMultipleHandCardsBeforeDrawing() {
        Forest firstHandCard = new Forest();
        Mountain secondHandCard = new Mountain();
        Mountain firstDrawnCard = new Mountain();
        Forest secondDrawnCard = new Forest();
        Mountain remainingLibraryCard = new Mountain();

        harness.addToBattlefield(player1, new Mindmoil());
        harness.setHand(player1, List.of(new BirdsOfParadise(), firstHandCard, secondHandCard));
        harness.setLibrary(player1, List.of(firstDrawnCard, secondDrawnCard, remainingLibraryCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(firstHandCard, secondHandCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDrawnCard, secondDrawnCard);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(remainingLibraryCard, secondHandCard, firstHandCard);
    }

    @Test
    @DisplayName("Casting the last card in hand does not draw any cards")
    void castingLastCardLeavesHandAndLibraryUnchanged() {
        Forest libraryCard = new Forest();

        harness.addToBattlefield(player1, new Mindmoil());
        harness.setHand(player1, List.of(new BirdsOfParadise()));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("An empty library is replenished before the ordered hand is drawn")
    void drawsBottomedCardsWhenLibraryWasEmpty() {
        Forest firstHandCard = new Forest();
        Mountain secondHandCard = new Mountain();

        harness.addToBattlefield(player1, new Mindmoil());
        harness.setHand(player1, List.of(new BirdsOfParadise(), firstHandCard, secondHandCard));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondHandCard, firstHandCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The hand is exchanged at resolution, including cards drawn after casting")
    void usesHandAtResolution() {
        Forest originalHandCard = new Forest();
        Mountain cardDrawnBeforeResolution = new Mountain();
        Forest firstReplacementCard = new Forest();
        Mountain secondReplacementCard = new Mountain();

        harness.addToBattlefield(player1, new Mindmoil());
        harness.setHand(player1, List.of(new BirdsOfParadise(), originalHandCard));
        harness.setLibrary(player1, List.of(cardDrawnBeforeResolution, firstReplacementCard, secondReplacementCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(originalHandCard);
        harness.getDrawService().resolveDrawCards(gd, player1.getId(), 1);
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(originalHandCard, cardDrawnBeforeResolution);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstReplacementCard, secondReplacementCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalHandCard, cardDrawnBeforeResolution);
    }

    @Test
    @DisplayName("Mindmoil does not trigger from its own casting")
    void doesNotTriggerForItsOwnCasting() {
        Forest handCard = new Forest();
        Mountain libraryCard = new Mountain();
        harness.setHand(player1, List.of(new Mindmoil(), handCard));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mindmoil");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Playing a land does not trigger Mindmoil")
    void playingLandDoesNotTrigger() {
        Forest land = new Forest();
        Mountain handCard = new Mountain();
        Forest libraryCard = new Forest();
        harness.addToBattlefield(player1, new Mindmoil());
        harness.setHand(player1, List.of(land, handCard));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("An opponent's spell does not trigger Mindmoil")
    void opponentSpellDoesNotTrigger() {
        Forest player1LibraryCard = new Forest();
        BirdsOfParadise opponentSpell = new BirdsOfParadise();
        Mountain player1HandCard = new Mountain();

        harness.addToBattlefield(player1, new Mindmoil());
        harness.setHand(player1, List.of(player1HandCard));
        harness.setLibrary(player1, List.of(player1LibraryCard));
        harness.setHand(player2, List.of(opponentSpell));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1HandCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1LibraryCard);
    }
}
