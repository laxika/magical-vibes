package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DAvenantHealer.class, GrizzlyBears.class})
class DAvenantHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target attacking creature")
    void damagesAttackingCreature() {
        Permanent healer = addReadyHealer();
        Permanent attacker = addReadyCreature(player2);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(healer.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 1 damage to target blocking creature")
    void damagesBlockingCreature() {
        addReadyHealer();
        Permanent blocker = addReadyCreature(player2);
        blocker.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void rejectsNoncombatCreature() {
        addReadyHealer();
        Permanent creature = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("Prevents the next 1 damage to a target creature")
    void preventsDamageToCreature() {
        addReadyHealer();
        Permanent creature = addReadyCreature(player2);

        activatePrevention(creature.getId());

        assertThat(creature.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents the next 1 damage to a target player")
    void preventsDamageToPlayer() {
        addReadyHealer();

        activatePrevention(player2.getId());

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    private Permanent addReadyHealer() {
        Permanent healer = harness.addToBattlefieldAndReturn(player1, new DAvenantHealer());
        healer.setSummoningSick(false);
        return healer;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private void activatePrevention(UUID targetId) {
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();
    }
}
