package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private void cast() {
        harness.setHand(player1, List.of(new FortunesFavor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
