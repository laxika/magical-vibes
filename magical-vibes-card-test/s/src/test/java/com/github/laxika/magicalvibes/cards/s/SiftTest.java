package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sift.class, GrizzlyBears.class})
class SiftTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sift puts it on the stack")
    void castingPutsOnStack() {
        Sift sift = new Sift();
        harness.castFromHand(player1, sift, "{3}{U}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isSameAs(sift);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Sift()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving draws three cards then prompts for discard")
    void resolvingDrawsThreeThenPromptsForDiscard() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new Sift(), "{3}{U}");
        harness.passBothPriorities();

        // The spell left hand, then three cards were drawn.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Completing discard results in net gain of two cards")
    void completingDiscardResultsInNetGainOfTwo() {
        harness.castFromHand(player1, new Sift(), "{3}{U}");
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sift goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Sift sift = new Sift();
        harness.castFromHand(player1, sift, "{3}{U}");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sift);
    }

    @Test
    @DisplayName("Can choose which card to discard")
    void canChooseWhichCardToDiscard() {
        Sift sift = new Sift();
        GrizzlyBears firstDraw = new GrizzlyBears();
        GrizzlyBears secondDraw = new GrizzlyBears();
        Sift discardedDraw = new Sift();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, discardedDraw));
        harness.castFromHand(player1, sift, "{3}{U}");
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sift, discardedDraw);
    }

    @Test
    @DisplayName("Can discard a card that was already in hand")
    void canDiscardCardAlreadyInHand() {
        Sift sift = new Sift();
        GrizzlyBears preexistingCard = new GrizzlyBears();
        GrizzlyBears firstDraw = new GrizzlyBears();
        GrizzlyBears secondDraw = new GrizzlyBears();
        GrizzlyBears thirdDraw = new GrizzlyBears();
        harness.setHand(player1, List.of(sift, preexistingCard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sift, preexistingCard);
    }

    @Test
    @DisplayName("Only the spell's controller draws and discards")
    void onlyControllerDrawsAndDiscards() {
        Sift sift = new Sift();
        GrizzlyBears firstDraw = new GrizzlyBears();
        GrizzlyBears secondDraw = new GrizzlyBears();
        GrizzlyBears discardedDraw = new GrizzlyBears();
        GrizzlyBears opponentCard = new GrizzlyBears();
        harness.setHand(player1, List.of(opponentCard));
        int opponentLibrarySize = gd.playerDecks.get(player1.getId()).size();
        harness.setLibrary(player2, List.of(firstDraw, secondDraw, discardedDraw));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, sift, "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactly(firstDraw, secondDraw, discardedDraw);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 2);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(sift, discardedDraw);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(opponentCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(opponentLibrarySize);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}

