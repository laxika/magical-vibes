package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VulshokReplicaTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Vulshok Replica deals 3 damage to target player")
    void dealsDamageToPlayer() {
        harness.addToBattlefield(player1, new VulshokReplica());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Vulshok Replica is sacrificed as part of the cost")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new VulshokReplica());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Vulshok Replica"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Cannot target a creature with the ability")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new VulshokReplica());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent elves = findPermanent(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elves.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player or planeswalker");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new VulshokReplica());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target self with the ability")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new VulshokReplica());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(name + " not found"));
    }
}
