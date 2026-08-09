package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TetheredGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when its controller controls no enchantments")
    void sacrificesWhenNoEnchantments() {
        castGriffin();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tethered Griffin");
        harness.assertInGraveyard(player1, "Tethered Griffin");
    }

    @Test
    @DisplayName("Survives while its controller controls an enchantment")
    void survivesWithEnchantment() {
        harness.addToBattlefield(player1, enchantment("Protective Enchantment"));
        castGriffin();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tethered Griffin");
    }

    @Test
    @DisplayName("An opponent's enchantment does not satisfy the condition")
    void opponentEnchantmentDoesNotCount() {
        harness.addToBattlefield(player2, enchantment("Opponent Enchantment"));
        castGriffin();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tethered Griffin");
        harness.assertInGraveyard(player1, "Tethered Griffin");
    }

    @Test
    @DisplayName("Gaining an enchantment after the ability triggers does not stop it")
    void conditionIsCheckedWhenAbilityTriggers() {
        castGriffin();

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, enchantment("Late Enchantment"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tethered Griffin");
        harness.assertInGraveyard(player1, "Tethered Griffin");
    }

    private void castGriffin() {
        harness.setHand(player1, List.of(new TetheredGriffin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
    }

    private Card enchantment(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        return card;
    }
}
