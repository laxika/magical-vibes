package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LazierGoblin.class, GrizzlyBears.class})
class LazierGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield deals 2 damage to any target")
    void entersAndDealsDamage() {
        harness.setHand(player1, List.of(new LazierGoblin()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot attack or block before being motivated")
    void cannotAttackOrBlockBeforeMotivation() {
        Permanent goblin = addCreatureReady(player1, new LazierGoblin());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(goblin.isAttacking()).isFalse();

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(goblin.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Paying motivate once allows the Goblin to attack")
    void motivationAllowsAttacking() {
        Permanent goblin = addCreatureReady(player1, new LazierGoblin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(goblin.isMotivated()).isTrue();
        declareAttackers(player1, List.of(0));

        assertThat(gd.creaturesAttackedCountThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
