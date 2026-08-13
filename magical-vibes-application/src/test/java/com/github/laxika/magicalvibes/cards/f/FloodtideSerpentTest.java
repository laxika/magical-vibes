package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FloodtideSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack without an enchantment to return")
    void cannotAttackWithoutEnchantment() {
        addCreatureReady(player1, new FloodtideSerpent());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns a controlled enchantment to its owner's hand when attacking")
    void returnsEnchantmentToHandWhenAttacking() {
        addCreatureReady(player1, new FloodtideSerpent());
        harness.addToBattlefield(player1, new GloriousAnthem());

        declareAttackers(player1, List.of(0));

        harness.assertInHand(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot use an enchantment controlled by an opponent")
    void cannotUseOpponentsEnchantment() {
        addCreatureReady(player1, new FloodtideSerpent());
        harness.addToBattlefield(player2, new GloriousAnthem());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
