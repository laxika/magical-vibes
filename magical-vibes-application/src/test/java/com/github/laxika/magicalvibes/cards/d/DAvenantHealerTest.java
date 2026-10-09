package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DAvenantHealer.class, AshcoatBear.class, Island.class})
class DAvenantHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target attacking creature")
    void damagesAttackingCreature() {
        Permanent healer = addCreatureReady(player1, new DAvenantHealer());
        Permanent attacker = addCreatureReady(player2, new AshcoatBear());
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
        addCreatureReady(player1, new DAvenantHealer());
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());
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
        addCreatureReady(player1, new DAvenantHealer());
        Permanent creature = addCreatureReady(player2, new AshcoatBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("The damage ability cannot target a player")
    void rejectsPlayerTarget() {
        addCreatureReady(player1, new DAvenantHealer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles when its target stops attacking before resolution")
    void fizzlesWhenTargetStopsAttacking() {
        addCreatureReady(player1, new DAvenantHealer());
        Permanent attacker = addCreatureReady(player2, new AshcoatBear());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents the next 1 damage to a target creature")
    void preventsDamageToCreature() {
        addCreatureReady(player1, new DAvenantHealer());
        Permanent creature = addCreatureReady(player2, new AshcoatBear());

        activatePrevention(creature.getId());

        assertThat(creature.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents the next 1 damage to a target player")
    void preventsDamageToPlayer() {
        addCreatureReady(player1, new DAvenantHealer());

        activatePrevention(player2.getId());

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents only the next 1 damage to the target player")
    void preventsOnlyNextDamageToPlayer() {
        addCreatureReady(player1, new DAvenantHealer());
        Permanent attacker = addCreatureReady(player1, new AshcoatBear());
        harness.setLife(player2, 20);

        activatePrevention(player2.getId());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();

        harness.assertLife(player2, 19);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Prevention shield expires at the end of the turn")
    void preventionShieldExpiresAtEndOfTurn() {
        addCreatureReady(player1, new DAvenantHealer());

        activatePrevention(player2.getId());
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("The prevention ability cannot target a land")
    void rejectsLandTarget() {
        addCreatureReady(player1, new DAvenantHealer());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature shield prevents one damage and is consumed before the next damage event")
    void creatureShieldIsConsumedByFirstDamageEvent() {
        addCreatureReady(player1, new DAvenantHealer());
        addCreatureReady(player1, new DAvenantHealer());
        addCreatureReady(player1, new DAvenantHealer());
        Permanent attacker = addCreatureReady(player2, new AshcoatBear());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            activatePrevention(attacker.getId());

            harness.activateAbility(player1, 1, null, attacker.getId());
            harness.passBothPriorities();
            assertThat(attacker.getMarkedDamage()).isZero();
            assertThat(attacker.getDamagePreventionShield()).isZero();

            harness.activateAbility(player1, 2, null, attacker.getId());
            harness.passBothPriorities();
            assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Activating prevention pays the tap cost and prevents using the damage ability")
    void preventionTapsHealerAndPrecludesDamageAbility() {
        Permanent healer = addCreatureReady(player1, new DAvenantHealer());
        Permanent attacker = addCreatureReady(player2, new AshcoatBear());
        attacker.setAttacking(true);

        activatePrevention(player1.getId());

        assertThat(healer.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Neither tap ability can be activated while the healer has summoning sickness")
    void summoningSicknessPreventsBothAbilities() {
        Permanent healer = harness.addToBattlefieldAndReturn(player1, new DAvenantHealer());
        healer.setSummoningSick(true);
        Permanent attacker = addCreatureReady(player2, new AshcoatBear());
        attacker.setAttacking(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(healer.isTapped()).isFalse();
    }

    private void activatePrevention(UUID targetId) {
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();
    }
}
