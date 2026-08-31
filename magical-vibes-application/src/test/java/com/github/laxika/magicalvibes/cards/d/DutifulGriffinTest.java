package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DutifulGriffin.class, DuelingGrounds.class, GhostlyPrison.class, Spellbook.class})
class DutifulGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard to hand by sacrificing two enchantments")
    void returnsFromGraveyardBySacrificingTwoEnchantments() {
        harness.setGraveyard(player1, List.of(new DutifulGriffin()));
        harness.addToBattlefield(player1, new DuelingGrounds());
        harness.addToBattlefield(player1, new GhostlyPrison());
        addReturnMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dutiful Griffin");
        harness.assertInGraveyard(player1, "Dueling Grounds");
        harness.assertInGraveyard(player1, "Ghostly Prison");
    }

    @Test
    @DisplayName("Cannot activate without two enchantments")
    void requiresTwoEnchantments() {
        harness.setGraveyard(player1, List.of(new DutifulGriffin()));
        harness.addToBattlefield(player1, new DuelingGrounds());
        harness.addToBattlefield(player1, new Spellbook());
        addReturnMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents");

        harness.assertInGraveyard(player1, "Dutiful Griffin");
    }

    private void addReturnMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
