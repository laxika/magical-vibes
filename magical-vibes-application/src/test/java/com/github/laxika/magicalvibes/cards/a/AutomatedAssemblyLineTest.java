package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AutomatedAssemblyLine.class, Memnite.class, GrizzlyBears.class})
class AutomatedAssemblyLineTest extends BaseCardTest {

    @Test
    void gainsOneEnergyWhenAnArtifactCreatureDealsCombatDamage() {
        harness.addToBattlefield(player1, new AutomatedAssemblyLine());
        addAttacker(new Memnite());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void batchesMultipleArtifactCreatureDealersIntoOneEnergy() {
        harness.addToBattlefield(player1, new AutomatedAssemblyLine());
        addAttacker(new Memnite());
        addAttacker(new Memnite());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void nonArtifactCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new AutomatedAssemblyLine());
        addAttacker(new GrizzlyBears());

        resolveCombat();

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

    @Test
    void opponentArtifactCreatureDoesNotGiveControllerEnergy() {
        harness.addToBattlefield(player1, new AutomatedAssemblyLine());
        Permanent attacker = addCreatureReady(player2, new Memnite());
        attacker.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void eachAssemblyLineTriggersIndependently() {
        harness.addToBattlefield(player1, new AutomatedAssemblyLine());
        harness.addToBattlefield(player1, new AutomatedAssemblyLine());
        addAttacker(new Memnite());
        addAttacker(new Memnite());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void paysEnergyImmediatelyAndCanActivateTwiceWithoutTapping() {
        Permanent assemblyLine = addAssemblyLine();
        assemblyLine.setTapped(true);
        gd.playerEnergyCounters.put(player1.getId(), 6);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(assemblyLine);

        harness.activateAbility(player1, index, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());

        harness.activateAbility(player1, index, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList())
                .hasSize(2).allMatch(Permanent::isTapped);
    }

    @Test
    void createdRobotCanGenerateEnergyThroughCombat() {
        Permanent assemblyLine = addAssemblyLine();
        gd.playerEnergyCounters.put(player1.getId(), 3);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(assemblyLine);
        harness.activateAbility(player1, index, null, null);
        resolveAllTriggers();
        Permanent robot = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        robot.setTapped(false);
        robot.setSummoningSick(false);
        robot.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = addCreatureReady(player1, card);
        permanent.setAttacking(true);
        return permanent;
    }

    private Permanent addAssemblyLine() {
        return harness.addToBattlefieldAndReturn(player1, new AutomatedAssemblyLine());
    }

}
