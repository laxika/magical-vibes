package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FortunesFavor.class, Island.class, Forest.class, Swamp.class, Plains.class, Mountain.class})
class FortunesFavorTest extends BaseCardTest {

    @Test
    @DisplayName("The targeted opponent looks at the top four cards and separates the piles")
    void targetedOpponentSeparatesTopFour() {
        List<Card> library = List.of(new Island(), new Forest(), new Swamp(), new Plains(), new Mountain());
        harness.setLibrary(player1, library);

        cast();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                library.get(0).getId(), library.get(1).getId(), library.get(2).getId(), library.get(3).getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(4));
        assertThat(gd.peekPendingInteraction(PendingPileSeparation.class).targetPlayerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The controller chooses between the face-down and face-up piles")
    void controllerChoosesPileAndMovesCardsToHandAndGraveyard() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(island, forest, swamp, plains));

        cast();

        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), forest.getId()));

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.description()).contains("2 cards", swamp.getName(), plains.getName())
                .doesNotContain(island.getName(), forest.getName());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(island, forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(swamp, plains)
                .anyMatch(card -> card.getName().equals("Fortune's Favor"));
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("The spell cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new FortunesFavor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .hasMessageContaining("opponent");
    }


    @Test
    @DisplayName("Choosing the face-up pile sends the face-down pile to the graveyard")
    void choosesFaceUpPile() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(island, forest, swamp, plains));

        cast();
        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), forest.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(swamp, plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest)
                .doesNotContain(swamp, plains);
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("The controller can choose an empty face-down pile")
    void choosesEmptyFaceDownPile() {
        List<Card> library = List.of(new Island(), new Forest(), new Swamp(), new Plains());
        harness.setLibrary(player1, library);

        cast();
        harness.handleMultipleCardsChosen(player2, List.of());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(library);
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("The opponent can put every card into the face-down pile")
    void choosesAllCardsInFaceDownPile() {
        List<Card> library = List.of(new Island(), new Forest(), new Swamp(), new Plains());
        harness.setLibrary(player1, library);

        cast();
        harness.handleMultipleCardsChosen(player2, library.stream().map(Card::getId).toList());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContainAnyElementsOf(library);
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("A short library uses all remaining cards without causing a draw loss")
    void separatesShortLibrary() {
        Card island = new Island();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(island, forest));

        cast();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(island.getId(), forest.getId());
        harness.handleMultipleCardsChosen(player2, List.of(island.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island).doesNotContain(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("An empty library resolves without prompting for a pile")
    void emptyLibraryResolvesWithoutChoice() {
        harness.setLibrary(player1, List.of());

        cast();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
        harness.assertInGraveyard(player1, "Fortune's Favor");
    }

    private void cast() {
        harness.setHand(player1, List.of(new FortunesFavor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }
}
