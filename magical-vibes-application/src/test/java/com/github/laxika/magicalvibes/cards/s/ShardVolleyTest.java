package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShardVolleyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting sacrifices a land and puts spell on the stack")
    void castSacrificesLand() {
        Permanent land = new Permanent(new Mountain());
        gd.playerBattlefields.get(player1.getId()).add(land);

        harness.setHand(player1, List.of(new ShardVolley()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), land.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Resolving deals 3 damage to any target player")
    void resolvingDeals3DamageToPlayer() {
        Permanent land = new Permanent(new Mountain());
        gd.playerBattlefields.get(player1.getId()).add(land);

        harness.setHand(player1, List.of(new ShardVolley()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17); // 20 - 3
    }

    @Test
    @DisplayName("Resolving deals 3 damage to a target creature")
    void resolvingDeals3DamageToCreature() {
        Permanent land = new Permanent(new Mountain());
        gd.playerBattlefields.get(player1.getId()).add(land);

        Permanent creature = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new ShardVolley()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithSacrifice(player1, 0, creature.getId(), land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot cast without a land to sacrifice")
    void cannotCastWithoutLand() {
        harness.setHand(player1, List.of(new ShardVolley()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-land permanent")
    void cannotSacrificeNonLand() {
        Permanent creature = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(creature);

        harness.setHand(player1, List.of(new ShardVolley()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
