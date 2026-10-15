package com.github.laxika.magicalvibes.cards.s;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({SanityGrinding.class, StreamHopper.class, NettleSentinel.class})
class SanityGrindingTest extends BaseCardTest {

    /** Ten cards whose blue mana symbols sum to seven: 2x {U}{U}{U} + 1x {U/R} + 7x {G}. */
    private List<Card> librarySevenBlueSymbols() {
        List<Card> library = new ArrayList<>();
        library.add(new SanityGrinding()); // {U}{U}{U} = 3
        library.add(new SanityGrinding()); // {U}{U}{U} = 3
        library.add(new StreamHopper());   // {U/R}     = 1
        IntStream.range(0, 7).forEach(i -> library.add(new NettleSentinel())); // {G} = 0
        return library;
    }

    private void cast() {
        harness.setHand(player1, List.of(new SanityGrinding()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Target opponent mills one card per blue mana symbol among the revealed cards")
    void millsPerBlueSymbol() {
        harness.setLibrary(player1, librarySevenBlueSymbols());

        cast();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("No mill when the revealed cards contain no blue mana symbols")
    void noMillWithoutBlueSymbols() {
        List<Card> library = new ArrayList<>();
        IntStream.range(0, 10).forEach(i -> library.add(new NettleSentinel())); // {G} = 0
        harness.setLibrary(player1, library);

        cast();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A library with fewer than ten cards reveals and bottoms all available cards")
    void shortLibraryUsesAllAvailableCards() {
        Card blueCard = new StreamHopper();
        Card nonBlueCard = new NettleSentinel();
        harness.setLibrary(player1, List.of(blueCard, nonBlueCard));

        cast();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(blueCard, nonBlueCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonBlueCard, blueCard);
    }

    @Test
    @DisplayName("Revealed cards are put on the bottom of the caster's library in any order")
    void revealedCardsGoToBottom() {
        harness.setLibrary(player1, librarySevenBlueSymbols());

        cast();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(reorder).hasSize(10);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(IntStream.range(0, reorder.size()).boxed().toList()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(10);
    }

    @Test
    @DisplayName("Cannot target yourself — the target must be an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new SanityGrinding()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("An empty caster library causes no mill and no reorder choice")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());
        Card opponentCard = new NettleSentinel();
        harness.setLibrary(player2, List.of(opponentCard));

        cast();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A single revealed card is returned without a reorder choice")
    void singleCardIsBottomedAutomatically() {
        Card revealed = new StreamHopper();
        Card milled = new NettleSentinel();
        Card remaining = new StreamHopper();
        harness.setLibrary(player1, List.of(revealed));
        harness.setLibrary(player2, List.of(milled, remaining));

        cast();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(milled);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Mill is limited to the cards remaining in the opponent's library")
    void shortOpponentLibraryMillsAllAvailableCards() {
        harness.setLibrary(player1, List.of(new SanityGrinding()));
        Card first = new NettleSentinel();
        Card second = new StreamHopper();
        harness.setLibrary(player2, List.of(first, second));

        cast();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only the top ten cards count and they go below the unrevealed cards in the chosen order")
    void revealLimitAndBottomOrder() {
        List<Card> revealed = IntStream.range(0, 10)
                .mapToObj(i -> (Card) new NettleSentinel()).toList();
        Card unrevealed = new SanityGrinding();
        List<Card> library = new ArrayList<>(revealed);
        library.add(unrevealed);
        harness.setLibrary(player1, library);

        cast();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactlyElementsOf(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed);

        List<Integer> order = IntStream.range(0, 10).map(i -> 9 - i).boxed().toList();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(order));

        List<Card> expected = new ArrayList<>();
        expected.add(unrevealed);
        order.forEach(i -> expected.add(revealed.get(i)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(expected);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
