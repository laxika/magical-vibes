package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FaerieConclave;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncroachTest extends BaseCardTest {

    @Test
    @DisplayName("Only a nonbasic land card can be chosen from the revealed hand")
    void onlyNonbasicLandCanBeChosen() {
        castEncroachAt(new ArrayList<>(List.of(new Forest(), new FaerieConclave(), new Peek())));

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Faerie Conclave");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Peek");
    }

    @Test
    @DisplayName("A basic land cannot be chosen")
    void basicLandCannotBeChosen() {
        castEncroachAt(new ArrayList<>(List.of(new Forest())));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("A non-land card cannot be chosen")
    void nonLandCannotBeChosen() {
        castEncroachAt(new ArrayList<>(List.of(new Peek(), new FaerieConclave())));

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("The spell can target its controller")
    void canTargetController() {
        harness.setHand(player1, new ArrayList<>(List.of(new Encroach(), new FaerieConclave())));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class))
                .isNotNull();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Faerie Conclave");
    }

    private void castEncroachAt(List<com.github.laxika.magicalvibes.model.Card> targetHand) {
        harness.setHand(player2, targetHand);
        harness.setHand(player1, List.of(new Encroach()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
