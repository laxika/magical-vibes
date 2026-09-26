package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LadyCaleria.class, DurkwoodBoars.class})
class LadyCaleriaTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToAttackingCreature() {
        Permanent lady = addCreatureReady(player1, new LadyCaleria());
        Permanent attacker = addCreatureReady(player2, new DurkwoodBoars());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
        assertThat(lady.isTapped()).isTrue();
    }

    @Test
    void dealsThreeDamageToBlockingCreature() {
        addCreatureReady(player1, new LadyCaleria());
        Permanent blocker = addCreatureReady(player2, new DurkwoodBoars());
        blocker.setBlocking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void doesNotDamageCreatureThatStopsAttackingBeforeResolution() {
        addCreatureReady(player1, new LadyCaleria());
        Permanent attacker = addCreatureReady(player2, new DurkwoodBoars());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetNonCombatCreature() {
        Permanent lady = addCreatureReady(player1, new LadyCaleria());
        Permanent bystander = addCreatureReady(player2, new DurkwoodBoars());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
        assertThat(lady.isTapped()).isFalse();
    }
}
