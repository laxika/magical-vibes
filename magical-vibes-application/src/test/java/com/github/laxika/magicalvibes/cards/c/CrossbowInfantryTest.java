package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrossbowInfantry.class, FugitiveWizard.class, GrizzlyBears.class})
class CrossbowInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps the infantry and puts it on the stack")
    void activatingPutsOnStack() {
        Permanent infantry = addCreatureReady(player1, new CrossbowInfantry());
        Permanent attacker = addAttackingCreature(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(infantry.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Deals 1 damage — a 2-toughness attacker survives")
    void dealsOneDamageTargetSurvives() {
        addCreatureReady(player1, new CrossbowInfantry());
        Permanent attacker = addAttackingCreature(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(attacker.getId()));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("deals 1 damage"));
    }

    @Test
    @DisplayName("1 damage destroys a 1-toughness blocking creature")
    void destroysOneToughnessTarget() {
        addCreatureReady(player1, new CrossbowInfantry());
        Permanent blocker = addCreatureReady(player2, new FugitiveWizard());
        blocker.setBlocking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Ability fizzles if the target stops attacking before resolution")
    void fizzlesIfTargetStopsAttackingBeforeResolution() {
        addCreatureReady(player1, new CrossbowInfantry());
        Permanent attacker = addAttackingCreature(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        addCreatureReady(player1, new CrossbowInfantry());
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent creature = addCreatureReady(player, new GrizzlyBears());
        creature.setAttacking(true);
        return creature;
    }
}
