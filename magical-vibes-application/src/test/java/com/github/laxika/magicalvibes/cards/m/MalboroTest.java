package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Malboro.class, Forest.class, Swamp.class})
class MalboroTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes each opponent discard, lose 2 life, and exile the top three cards")
    void etbAppliesBadBreath() {
        Card discarded = new Malboro();
        Card remainingHandCard = new Forest();
        Card exiledTop = new Forest();
        Card exiledMiddle = new Swamp();
        Card exiledBottom = new Malboro();

        harness.setHand(player1, List.of(new Malboro()));
        harness.setHand(player2, new ArrayList<>(List.of(discarded, remainingHandCard)));
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(exiledTop, exiledMiddle, exiledBottom, new Malboro()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingHandCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactly(exiledTop, exiledMiddle, exiledBottom);
        harness.assertInGraveyard(player2, "Malboro");
    }

    @Test
    @DisplayName("Swampcycling discards Malboro and searches for a Swamp")
    void swampcyclingSearchesForSwamp() {
        harness.setHand(player1, List.of(new Malboro()));
        harness.setLibrary(player1, List.of(new Forest(), new Swamp(), new Malboro()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Malboro");
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).singleElement().extracting(Card::getName).isEqualTo("Swamp");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Swamp");
    }

    @Test
    @DisplayName("An empty hand does not stop life loss or exile from a short library")
    void emptyHandAndShortLibrary() {
        Card first = new Forest();
        Card second = new Swamp();
        Card ownHand = new Swamp();
        Card ownLibrary = new Forest();
        harness.setHand(player1, List.of(new Malboro(), ownHand));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(ownLibrary));
        harness.setLibrary(player2, List.of(first, second));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 18);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownLibrary);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Bad Breath waits for discard and still loses life with an empty library")
    void emptyLibraryDoesNotStopDiscardAndLifeLoss() {
        Card discarded = new Forest();
        Card kept = new Swamp();
        harness.setHand(player1, List.of(new Malboro()));
        harness.setHand(player2, List.of(discarded, kept));
        harness.setLibrary(player2, List.of());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Swampcycling discards as a cost and may fail to find an available Swamp")
    void swampcyclingMayFailToFind() {
        Card swamp = new Swamp();
        Card forest = new Forest();
        harness.setHand(player1, List.of(new Malboro()));
        harness.setLibrary(player1, List.of(swamp, forest));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Malboro");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(swamp, forest);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(swamp, forest);
        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Swampcycling resolves without a card when the library has no Swamp")
    void swampcyclingWithNoMatchingCard() {
        Card forest = new Forest();
        harness.setHand(player1, List.of(new Malboro()));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertInGraveyard(player1, "Malboro");
    }
}
