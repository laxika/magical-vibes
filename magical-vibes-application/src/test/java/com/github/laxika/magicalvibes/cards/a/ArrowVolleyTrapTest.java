package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArrowVolleyTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage divided among attacking creatures")
    void dealsDamageAmongAttackers() {
        Permanent first = addAttacker();
        Permanent second = addAttacker();
        harness.setHand(player1, List.of(new ArrowVolleyTrap()));
        addNormalMana();

        harness.castInstant(player1, 0, Map.of(first.getId(), 3, second.getId(), 2));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rejects non-attacking creatures")
    void rejectsNonAttackingCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArrowVolleyTrap()));
        addNormalMana();

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(creature.getId(), 5))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast for {1}{W} when four creatures are attacking")
    void castsForAlternateCostWithFourAttackers() {
        Permanent first = addAttacker();
        Permanent second = addAttacker();
        Permanent third = addAttacker();
        Permanent fourth = addAttacker();
        harness.setHand(player1, List.of(new ArrowVolleyTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        castWithAlternateCost(Map.of(
                first.getId(), 2,
                second.getId(), 1,
                third.getId(), 1,
                fourth.getId(), 1
        ));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Alternate cost requires four attacking creatures")
    void alternateCostRequiresFourAttackers() {
        Permanent first = addAttacker();
        Permanent second = addAttacker();
        Permanent third = addAttacker();
        harness.setHand(player1, List.of(new ArrowVolleyTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> castWithAlternateCost(Map.of(
                first.getId(), 2,
                second.getId(), 2,
                third.getId(), 1
        ))).isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        return attacker;
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void castWithAlternateCost(Map<UUID, Integer> damageAssignments) {
        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, damageAssignments, List.of());
    }
}
