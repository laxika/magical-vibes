package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UndercityPlunder.class, Forest.class})
class UndercityPlunderTest extends BaseCardTest {

    @Test
    void targetOpponentDiscardsThenMayDiscardAgain() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Forest())));
        cast();

        harness.handleCardChosen(player2, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNotNull();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningAdditionalDiscardConjuresRandomLibraryCardWithAnyColorCasting() {
        Forest libraryCard = new Forest();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Forest())));
        cast();

        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, false);

        Card conjured = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(conjured.getName()).isEqualTo(libraryCard.getName());
        assertThat(conjured.getId()).isNotEqualTo(libraryCard.getId());
        assertThat(conjured.getOwnerId()).isEqualTo(player1.getId());
        assertThat(gd.perpetualAnyColorManaForCastCardIds).contains(conjured.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
    }

    @Test
    void canOnlyTargetAnOpponent() {
        harness.setHand(player1, List.of(new UndercityPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void emptyHandCannotAvoidConjuringByAcceptingAnImpossibleDiscard() {
        UndercityPlunder libraryCard = new UndercityPlunder();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new UndercityPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player2, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName())
                .isEqualTo(libraryCard.getName());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void discardingLastCardCannotAvoidConjuringByAcceptingAnImpossibleDiscard() {
        UndercityPlunder libraryCard = new UndercityPlunder();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player2, List.of(new UndercityPlunder()));
        cast();
        harness.handleCardChosen(player2, 0);

        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player2, true);
        }

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName())
                .isEqualTo(libraryCard.getName());
    }

    @Test
    void decliningWithEmptyLibraryDoesNotConjureAnything() {
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new UndercityPlunder(), new UndercityPlunder()));
        cast();
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void conjuredSpellCanActuallyBeCastUsingOnlyGreenMana() {
        harness.setLibrary(player2, List.of(new UndercityPlunder()));
        harness.setHand(player2, List.of(new UndercityPlunder(), new UndercityPlunder()));
        cast();
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, false);
        Card conjured = gd.playerHands.get(player1.getId()).getFirst();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player2, false);
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(conjured);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
    private void cast() {
        harness.setHand(player1, List.of(new UndercityPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNotNull();
    }
}
