package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SamiteHealer.class, GrizzlyBears.class})
class SamiteHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts its target on the stack")
    void activatingPutsTargetOnStack() {
        addReadyHealer(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Activating ability requires an any-target choice")
    void activatingRequiresTarget() {
        addReadyHealer(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires a target");
    }

    @Test
    @DisplayName("Activating ability taps the Healer")
    void activatingTapsHealer() {
        Permanent healer = addReadyHealer(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(healer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability adds a prevention shield to the target player")
    void resolvingAddsShieldToTargetPlayer() {
        addReadyHealer(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.globalDamagePreventionShield).isZero();
    }

    @Test
    @DisplayName("Resolving ability adds a prevention shield to the target creature")
    void resolvingAddsShieldToTargetCreature() {
        addReadyHealer(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
        assertThat(gd.globalDamagePreventionShield).isZero();
    }

    @Test
    @DisplayName("Resolving ability logs a targeted prevention message")
    void resolvingLogsTargetedPrevention() {
        addReadyHealer(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gameLogContains("The next 1 damage" + " that would be dealt to " + player2.getUsername()))
                .isTrue();
    }

    @Test
    @DisplayName("A target creature's shield prevents its next combat damage")
    void targetCreatureShieldPreventsCombatDamage() {
        addReadyHealer(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        declareAttackers(player1, List.of(attackerIndex));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(blocker.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("A player's shield prevents only the next 1 damage dealt to that player")
    void targetPlayerShieldPreventsOnlyOneDamage() {
        addReadyHealer(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackers(player1, List.of(attackerIndex));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.globalDamagePreventionShield).isZero();
    }

    @Test
    @DisplayName("A player's shield does not prevent damage dealt to another player")
    void targetPlayerShieldDoesNotAffectAnotherPlayer() {
        addReadyHealer(player1);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        declareAttackers(player2, List.of(attackerIndex));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.globalDamagePreventionShield).isZero();
    }

    @Test
    @DisplayName("A target player's prevention shield is cleared at end of turn")
    void targetPlayerShieldClearedAtEndOfTurn() {
        addReadyHealer(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Targeted activation prevents the next 1 combat damage to its target player")
    void targetedActivationPreventsPlayerDamage() {
        Permanent healer = addReadyHealer(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(healer.isTapped()).isTrue();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackers(player1, List.of(attackerIndex));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    private Permanent addReadyHealer(Player player) {
        return addCreatureReady(player, new SamiteHealer());
    }
}
