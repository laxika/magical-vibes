package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VoyageHomeTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        addArtifacts(5);
        harness.setHand(player1, List.of(new VoyageHome()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot cast without enough artifacts or generic mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new VoyageHome()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Draws three cards and gains three life")
    void drawsThreeCardsAndGainsThreeLife() {
        addArtifacts(5);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new VoyageHome()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    private void addArtifacts(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Ornithopter());
        }
    }
}
