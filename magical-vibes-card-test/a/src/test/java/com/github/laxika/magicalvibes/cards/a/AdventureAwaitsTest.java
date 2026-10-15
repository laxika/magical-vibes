package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CanopyBaloth;
import com.github.laxika.magicalvibes.cards.r.RelicAxe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdventureAwaits.class, Forest.class, CanopyBaloth.class, RelicAxe.class})
class AdventureAwaitsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a revealed creature into hand and the rest on the bottom")
    void revealsCreatureIntoHand() {
        Card creature = new CanopyBaloth();
        Card firstNoncreature = new RelicAxe();
        Card secondNoncreature = new Forest();
        Card thirdNoncreature = new RelicAxe();
        Card fourthNoncreature = new Forest();
        List<Card> topCards = List.of(creature, firstNoncreature, secondNoncreature, thirdNoncreature,
                fourthNoncreature);
        castAdventureAwaits(topCards);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstNoncreature, secondNoncreature, thirdNoncreature,
                        fourthNoncreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Draws a card when the top five contain no creature")
    void drawsWhenNoCreatureIsFound() {
        Card firstNoncreature = new RelicAxe();
        Card secondNoncreature = new Forest();
        Card thirdNoncreature = new RelicAxe();
        Card fourthNoncreature = new Forest();
        Card fifthNoncreature = new RelicAxe();
        Card drawnCard = new CanopyBaloth();
        List<Card> topCards = List.of(firstNoncreature, secondNoncreature, thirdNoncreature,
                fourthNoncreature, fifthNoncreature);
        castAdventureAwaits(List.of(firstNoncreature, secondNoncreature, thirdNoncreature,
                fourthNoncreature, fifthNoncreature, drawnCard));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(topCards);
    }

    @Test
    @DisplayName("Declining an available creature draws from below the top five")
    void decliningCreatureDrawsAfterBottoming() {
        Card creature = new CanopyBaloth();
        List<Card> lookedAt = List.of(creature, new Forest(), new RelicAxe(), new Forest(), new RelicAxe());
        Card sixthCard = new Forest();
        castAdventureAwaits(List.of(lookedAt.get(0), lookedAt.get(1), lookedAt.get(2),
                lookedAt.get(3), lookedAt.get(4), sixthCard));

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sixthCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chooses only one creature and preserves the untouched library above the bottomed cards")
    void choosingCreaturePreservesUntouchedLibrary() {
        Card firstCreature = new CanopyBaloth();
        Card secondCreature = new CanopyBaloth();
        Card artifact = new RelicAxe();
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        Card sixthCard = new CanopyBaloth();
        Card seventhCard = new Forest();
        castAdventureAwaits(List.of(firstCreature, secondCreature, artifact, firstLand, secondLand,
                sixthCard, seventhCard));

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(firstCreature.getId(), secondCreature.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(secondCreature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondCreature);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2)).containsExactly(sixthCard, seventhCard);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 6))
                .containsExactlyInAnyOrder(firstCreature, artifact, firstLand, secondLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can take a creature from a library with fewer than five cards")
    void choosesFromShortLibrary() {
        Card creature = new CanopyBaloth();
        Card land = new Forest();
        castAdventureAwaits(List.of(creature, land));

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Draws the sole noncreature after putting it back on the bottom")
    void drawsFromShortLibraryAfterBottoming() {
        Card land = new Forest();
        castAdventureAwaits(List.of(land));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("An empty library still requires a draw and causes its controller to lose")
    void emptyLibraryStillAttemptsDraw() {
        castAdventureAwaits(List.of());

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    private void castAdventureAwaits(List<Card> topCards) {
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, topCards);
        harness.castFromHand(player1, new AdventureAwaits(), "{1}{G}");
        harness.passBothPriorities();
    }
}
