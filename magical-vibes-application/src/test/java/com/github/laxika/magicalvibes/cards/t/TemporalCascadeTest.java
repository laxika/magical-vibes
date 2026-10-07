package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemporalCascade.class, AlphaMyr.class})
class TemporalCascadeTest extends BaseCardTest {

    @Test
    @DisplayName("The shuffle mode shuffles each player's hand and graveyard into their library")
    void shufflesHandsAndGraveyards() {
        Card handCard = new AlphaMyr();
        Card graveyardCard = new AlphaMyr();
        harness.setHand(player1, List.of(new TemporalCascade()));
        harness.setHand(player2, List.of(handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));
        fillLibrary(player1, 20);
        fillLibrary(player2, 20);
        int expectedLibrarySize = gd.playerDecks.get(player2.getId()).size() + 2;
        addMana(5);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0}, List.of(), null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(handCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(graveyardCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(expectedLibrarySize);
    }

    @Test
    @DisplayName("The draw mode makes each player draw seven cards")
    void drawsSevenCards() {
        harness.setHand(player1, List.of(new TemporalCascade()));
        harness.setHand(player2, List.of());
        fillLibrary(player1, 20);
        fillLibrary(player2, 20);
        addMana(5);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1}, List.of(), null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Entwine resolves both modes and charges two additional mana")
    void entwineResolvesBothModes() {
        Card handCard = new AlphaMyr();
        Card graveyardCard = new AlphaMyr();
        harness.setHand(player1, List.of(new TemporalCascade()));
        harness.setHand(player2, List.of(handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));
        fillLibrary(player1, 20);
        fillLibrary(player2, 20);
        addMana(7);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1}, List.of(), null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(graveyardCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Entwine cannot be cast without its additional mana")
    void entwineRequiresAdditionalMana() {
        harness.setHand(player1, List.of(new TemporalCascade()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The shuffle mode also clears the caster's zones without shuffling the resolving spell")
    void shuffleModeClearsCasterZonesWithoutDrawing() {
        Card spell = new TemporalCascade();
        Card handCard = new AlphaMyr();
        Card graveyardCard = new AlphaMyr();
        harness.setHand(player1, List.of(spell, handCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of());
        fillLibrary(player1, 0);
        fillLibrary(player2, 0);
        addMana(5);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0}, List.of(), null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(handCard, graveyardCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The draw mode preserves existing hands and graveyards")
    void drawModeDoesNotShuffleZones() {
        Card casterHandCard = new AlphaMyr();
        Card opponentHandCard = new AlphaMyr();
        Card casterGraveyardCard = new AlphaMyr();
        Card opponentGraveyardCard = new AlphaMyr();
        harness.setHand(player1, List.of(new TemporalCascade(), casterHandCard));
        harness.setHand(player2, List.of(opponentHandCard));
        harness.setGraveyard(player1, List.of(casterGraveyardCard));
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));
        fillLibrary(player1, 7);
        fillLibrary(player2, 7);
        addMana(5);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1}, List.of(), null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(8).contains(casterHandCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(8).contains(opponentHandCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(casterGraveyardCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGraveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Entwine shuffles before drawing even when modes are supplied in reverse order")
    void entwineShufflesBeforeDrawingFromInitiallyEmptyLibraries() {
        Card spell = new TemporalCascade();
        List<Card> casterCards = IntStream.range(0, 7).mapToObj(i -> (Card) new AlphaMyr()).toList();
        List<Card> opponentCards = IntStream.range(0, 7).mapToObj(i -> (Card) new AlphaMyr()).toList();
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, opponentCards);
        harness.setGraveyard(player1, casterCards);
        harness.setGraveyard(player2, List.of());
        fillLibrary(player1, 0);
        fillLibrary(player2, 0);
        addMana(7);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1, 0}, List.of(), null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(casterCards);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrderElementsOf(opponentCards);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    private void addMana(int colorless) {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
    }

    private void fillLibrary(Player player, int count) {
        harness.setLibrary(player, IntStream.range(0, count)
                .mapToObj(i -> (Card) new AlphaMyr())
                .toList());
    }
}
