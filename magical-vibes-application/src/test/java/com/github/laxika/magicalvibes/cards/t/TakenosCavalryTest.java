package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FrostOgre;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TakenosCavalry.class, KamiOfFalseHope.class, FrostOgre.class})
class TakenosCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to an attacking Spirit")
    void damagesAttackingSpirit() {
        addCreatureReady(player1, new TakenosCavalry());
        Permanent spirit = addCreatureReady(player2, new KamiOfFalseHope());
        spirit.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, spirit.getId());
        harness.passBothPriorities();

        assertThat(spirit.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 1 damage to a blocking Spirit")
    void damagesBlockingSpirit() {
        addCreatureReady(player1, new TakenosCavalry());
        Permanent spirit = addCreatureReady(player2, new KamiOfFalseHope());
        spirit.setBlocking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, 0, spirit.getId());
        harness.passBothPriorities();

        assertThat(spirit.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a Spirit that is neither attacking nor blocking")
    void cannotTargetIdleSpirit() {
        addCreatureReady(player1, new TakenosCavalry());
        Permanent spirit = addCreatureReady(player2, new KamiOfFalseHope());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, spirit.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Spirit");
    }

    @Test
    @DisplayName("Cannot target an attacking non-Spirit creature")
    void cannotTargetNonSpirit() {
        addCreatureReady(player1, new TakenosCavalry());
        Permanent bears = addCreatureReady(player2, new FrostOgre());
        bears.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Spirit");
    }

    @Test
    @DisplayName("Ability fizzles if its Spirit target stops attacking before resolution")
    void abilityFizzlesWhenTargetStopsAttacking() {
        addCreatureReady(player1, new TakenosCavalry());
        Permanent spirit = addCreatureReady(player2, new KamiOfFalseHope());
        spirit.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, spirit.getId());
        spirit.setAttacking(false);
        harness.passBothPriorities();

        assertThat(spirit.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Bushido 1 gives +1/+1 when it becomes blocked")
    void bushidoOnBecomingBlocked() {
        Permanent attacker = addCreatureReady(player1, new TakenosCavalry());
        addCreatureReady(player2, new KamiOfFalseHope());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bushido 1 gives +1/+1 when it blocks")
    void bushidoOnBlocking() {
        addCreatureReady(player1, new KamiOfFalseHope());
        Permanent blocker = addCreatureReady(player2, new TakenosCavalry());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(1);
        assertThat(blocker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bushido 1 bonus wears off at end of turn")
    void bushidoWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new TakenosCavalry());
        addCreatureReady(player2, new KamiOfFalseHope());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }
}
