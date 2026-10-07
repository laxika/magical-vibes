package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrainOfThought.class, Gristleback.class})
class TrainOfThoughtTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card")
    void drawsACard() {
        harness.setHand(player1, List.of(new TrainOfThought()));
        harness.setLibrary(player1, List.of(new Gristleback()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Replicate draws one card for each copy and the original")
    void replicateDrawsForEachCopy() {
        harness.setHand(player1, List.of(new TrainOfThought()));
        harness.setLibrary(player1, List.of(new Gristleback(), new Gristleback(), new Gristleback()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorceryWithRepeatedCosts(player1, 0, List.of("{1}{U}", "{1}{U}"), List.of());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Replicate payments require the full {1}{U} cost")
    void cannotPayReplicateWithoutEnoughMana() {
        harness.setHand(player1, List.of(new TrainOfThought()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorceryWithRepeatedCosts(
                player1, 0, List.of("{1}{U}"), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Replicate requires blue mana for each payment")
    void cannotPayReplicateWithOnlyOneBlueMana() {
        harness.setHand(player1, List.of(new TrainOfThought()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorceryWithRepeatedCosts(
                player1, 0, List.of("{1}{U}"), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Replicate copies resolve separately before the original and do not remain in the graveyard")
    void replicateCopiesResolveIndependently() {
        TrainOfThought original = new TrainOfThought();
        Gristleback first = new Gristleback();
        Gristleback second = new Gristleback();
        Gristleback third = new Gristleback();
        harness.setHand(player1, List.of(original));
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorceryWithRepeatedCosts(player1, 0, List.of("{1}{U}", "{1}{U}"), List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(original);
        assertThat(gd.stack).isEmpty();
    }
}
