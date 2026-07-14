package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinWarrensTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing two Goblins creates three 1/1 red Goblin tokens")
    void createsThreeGoblinTokens() {
        Permanent warrens = harness.addToBattlefieldAndReturn(player1, new GoblinWarrens());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new RagingGoblin());

        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, indexOf(warrens), null, null);
        harness.passBothPriorities();

        // Both source Goblins sacrificed, three token Goblins created.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Raging Goblin"))
                .hasSize(2);

        var goblinTokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin"))
                .toList();
        assertThat(goblinTokens).hasSize(3);
        assertThat(goblinTokens).allSatisfy(p -> {
            assertThat(p.getCard().getPower()).isEqualTo(1);
            assertThat(p.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Cannot activate without two Goblins to sacrifice")
    void cannotActivateWithoutTwoGoblins() {
        Permanent warrens = harness.addToBattlefieldAndReturn(player1, new GoblinWarrens());
        harness.addToBattlefield(player1, new RagingGoblin());

        harness.addMana(player1, ManaColor.RED, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(warrens), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }
}
