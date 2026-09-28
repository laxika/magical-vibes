package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AutomatedAssemblyLine.class, Memnite.class, GrizzlyBears.class})
class AutomatedAssemblyLineTest extends BaseCardTest {

    @Test
    void gainsOneEnergyWhenAnArtifactCreatureDealsCombatDamage() {
        harness.addToBattlefield(player1, new AutomatedAssemblyLine());
        addAttacker(new Memnite());

        runCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void batchesMultipleArtifactCreatureDealersIntoOneEnergy() {
        harness.addToBattlefield(player1, new AutomatedAssemblyLine());
        addAttacker(new Memnite());
        addAttacker(new Memnite());

        runCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void nonArtifactCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new AutomatedAssemblyLine());
        addAttacker(new GrizzlyBears());

        runCombatDamage();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void paysThreeEnergyToCreateTappedRobot() {
        Permanent assemblyLine = addAssemblyLine();
        gd.playerEnergyCounters.put(player1.getId(), 3);

        int assemblyLineIndex = gd.playerBattlefields.get(player1.getId()).indexOf(assemblyLine);
        harness.activateAbility(player1, assemblyLineIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        Permanent robot = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(robot.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(3);
    }

    @Test
    void cannotActivateWithoutThreeEnergyCounters() {
        Permanent assemblyLine = addAssemblyLine();

        int assemblyLineIndex = gd.playerBattlefields.get(player1.getId()).indexOf(assemblyLine);
        assertThatThrownBy(() -> harness.activateAbility(player1, assemblyLineIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three energy counters");
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private Permanent addAssemblyLine() {
        Permanent permanent = new Permanent(new AutomatedAssemblyLine());
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void runCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
