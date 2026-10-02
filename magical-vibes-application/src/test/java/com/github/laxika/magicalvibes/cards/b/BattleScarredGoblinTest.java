package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattleScarredGoblin.class, GrizzlyBears.class})
class BattleScarredGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked deals 1 damage to each creature blocking it")
    void damagesEachCreatureBlockingIt() {
        Permanent goblin = addAttackingGoblin(player1);
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(goblin.getMarkedDamage()).isZero();
        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(1);
        assertThat(secondBlocker.getMarkedDamage()).isEqualTo(1);
        assertThat(bystander.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Becoming blocked triggers once with multiple blockers")
    void triggersOnceWithMultipleBlockers() {
        addAttackingGoblin(player1);
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(1);
        assertThat(secondBlocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not damage a creature blocking another attacker")
    void doesNotDamageBlockerOfAnotherAttacker() {
        addAttackingGoblin(player1);
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent goblinBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherBlocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));
        harness.passBothPriorities();

        assertThat(goblinBlocker.getMarkedDamage()).isEqualTo(1);
        assertThat(otherBlocker.getMarkedDamage()).isZero();
        assertThat(otherAttacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not trigger when unblocked")
    void doesNotTriggerWhenUnblocked() {
        Permanent goblin = addAttackingGoblin(player1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(goblin.getMarkedDamage()).isZero();
    }

    private Permanent addAttackingGoblin(Player player) {
        Permanent permanent = addCreatureReady(player, new BattleScarredGoblin());
        permanent.setAttacking(true);
        return permanent;
    }
}
