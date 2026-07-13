package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DreamSalvageTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards equal to the number of cards target opponent discarded this turn")
    void drawsEqualToOpponentDiscards() {
        // Opponent discards two cards this turn (Mind Rot).
        harness.setHand(player2, List.of(new GrizzlyBears(), new ScatheZombies()));
        harness.setHand(player1, List.of(new MindRot(), new DreamSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        // Dream Salvage draws two — one per card discarded this turn.
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws nothing when target opponent has discarded no cards this turn")
    void drawsNothingWithoutDiscards() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new ScatheZombies()));
        harness.setHand(player1, List.of(new DreamSalvage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new DreamSalvage()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
