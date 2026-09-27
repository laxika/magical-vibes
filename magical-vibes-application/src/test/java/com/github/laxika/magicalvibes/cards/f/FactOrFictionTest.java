package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FactOrFiction.class, Island.class, Forest.class, Swamp.class, Plains.class, Mountain.class})
class FactOrFictionTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals the top five cards and prompts an opponent to separate them")
    void revealsTopFiveAndPromptsOpponent() {
        List<Card> cards = List.of(new Island(), new Forest(), new Swamp(), new Plains(), new Mountain());
        harness.setLibrary(player1, cards);

        cast();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isTrue();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactlyInAnyOrderElementsOf(
                cards.stream().map(Card::getId).toList());
    }

    @Test
    @DisplayName("Puts the chosen pile into hand and the other pile into the graveyard")
    void chosenPileToHandOtherPileToGraveyard() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        Card mountain = new Mountain();
        harness.setLibrary(player1, List.of(island, forest, swamp, plains, mountain));

        cast();

        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), forest.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(island, forest);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(swamp, plains, mountain)
                .anyMatch(card -> card.getName().equals("Fact or Fiction"));
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("Can choose the second pile for hand")
    void chosenSecondPileToHandOtherPileToGraveyard() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        Card mountain = new Mountain();
        harness.setLibrary(player1, List.of(island, forest, swamp, plains, mountain));

        cast();

        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), forest.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(swamp, plains, mountain);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(island, forest)
                .anyMatch(card -> card.getName().equals("Fact or Fiction"));
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("Reveals all available cards and allows an empty pile")
    void revealsAllAvailableCardsAndAllowsEmptyPile() {
        Card island = new Island();
        harness.setLibrary(player1, List.of(island));

        cast();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(island.getId());

        harness.handleMultipleCardsChosen(player2, List.of());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(island);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island);
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("Resolves without a pile choice when the library is empty")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        cast();

        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Fact or Fiction"));
    }

    private void cast() {
        harness.castFromHand(player1, new FactOrFiction(), "{3}{U}");
        harness.passBothPriorities();
    }
}
