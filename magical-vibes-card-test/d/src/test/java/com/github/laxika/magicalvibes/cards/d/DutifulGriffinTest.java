package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    @DisplayName("Returns only the Griffin whose ability was activated, after paying sacrifices immediately")
    void returnsOnlyTheActivatedGriffin() {
        DutifulGriffin first = new DutifulGriffin();
        DutifulGriffin second = new DutifulGriffin();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addToBattlefield(player1, new DuelingGrounds());
        harness.addToBattlefield(player1, new DuelingGrounds());
        addReturnMana();

        harness.activateGraveyardAbility(player1, 1);

        harness.assertNotOnBattlefield(player1, "Dueling Grounds");
        harness.assertInGraveyard(player1, "Dueling Grounds");
        harness.assertNotInHand(player1, "Dutiful Griffin");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(second).doesNotContain(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second);
        harness.assertNotOnBattlefield(player1, "Dutiful Griffin");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's enchantment to pay the return cost")
    void cannotUseOpponentsEnchantments() {
        harness.setGraveyard(player1, List.of(new DutifulGriffin()));
        harness.addToBattlefield(player1, new DuelingGrounds());
        harness.addToBattlefield(player2, new DuelingGrounds());
        addReturnMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents");

        harness.assertOnBattlefield(player1, "Dueling Grounds");
        harness.assertOnBattlefield(player2, "Dueling Grounds");
        harness.assertInGraveyard(player1, "Dutiful Griffin");
        harness.assertNotInHand(player1, "Dutiful Griffin");
    }

    @Test
    @DisplayName("Cannot pay the return cost with generic mana alone")
    void requiresWhiteMana() {
        harness.setGraveyard(player1, List.of(new DutifulGriffin()));
        harness.addToBattlefield(player1, new DuelingGrounds());
        harness.addToBattlefield(player1, new DuelingGrounds());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Dueling Grounds");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Dutiful Griffin");
        harness.assertNotInHand(player1, "Dutiful Griffin");
    }
    private void addReturnMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
