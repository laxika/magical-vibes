package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(WaywardSoul.class)
class WaywardSoulTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {U} puts Wayward Soul on top of its owner's library")
    void activatePutsOnTopOfLibrary() {
        harness.addToBattlefield(player1, new WaywardSoul());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wayward Soul");
        harness.assertNotInHand(player1, "Wayward Soul");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(WaywardSoul.class);
    }

    @Test
    @DisplayName("Ability cannot be activated without paying {U}")
    void requiresMana() {
        harness.addToBattlefield(player1, new WaywardSoul());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot be activated with non-blue mana")
    void requiresBlueMana() {
        harness.addToBattlefield(player1, new WaywardSoul());

        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
