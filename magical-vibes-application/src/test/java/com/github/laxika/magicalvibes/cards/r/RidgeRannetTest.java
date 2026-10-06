package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RidgeRannet.class, CylianElf.class})
class RidgeRannetTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new RidgeRannet()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Ridge Rannet");
        harness.assertInHand(player1, "Cylian Elf");
    }

    @Test
    @DisplayName("Cycling pays the discard cost before the draw resolves")
    void cyclingDiscardsBeforeDrawing() {
        harness.setHand(player1, List.of(new RidgeRannet()));
        harness.setLibrary(player1, List.of(new CylianElf(), new CylianElf()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Ridge Rannet");
        harness.assertNotInHand(player1, "Ridge Rannet");
        harness.assertNotInHand(player1, "Cylian Elf");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Cylian Elf");
    }

    @Test
    @DisplayName("Cycling cannot be activated with only one mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new RidgeRannet()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertInHand(player1, "Ridge Rannet");
        harness.assertNotInHand(player1, "Cylian Elf");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
