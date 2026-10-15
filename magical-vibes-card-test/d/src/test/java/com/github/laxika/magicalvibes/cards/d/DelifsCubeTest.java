package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.IcatianPhalanx;
import com.github.laxika.magicalvibes.cards.q.QuestingBeast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DelifsCube.class, IcatianPhalanx.class, QuestingBeast.class})
class DelifsCubeTest extends BaseCardTest {

    @Test
    @DisplayName("An unblocked chosen attacker adds a cube counter and assigns no combat damage")
    void unblockedChosenAttackerAddsCubeCounterAndDealsNoCombatDamage() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new DelifsCube());
        Permanent attacker = addCreatureReady(player1, new IcatianPhalanx());
        addCreatureReady(player2, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        declareBlockers(List.of());

        assertThat(cube.getCounterCount(CounterType.CUBE)).isEqualTo(1);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A blocked chosen attacker does not add a cube counter or lose combat damage")
    void blockedChosenAttackerDoesNotTrigger() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new DelifsCube());
        Permanent attacker = addCreatureReady(player1, new IcatianPhalanx());
        Permanent blocker = addCreatureReady(player2, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareBlockers(List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(cube.getCounterCount(CounterType.CUBE)).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Removing a cube counter regenerates a target creature")
    void removingCubeCounterRegeneratesTargetCreature() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new DelifsCube());
        cube.setCounterCount(CounterType.CUBE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(cube.getCounterCount(CounterType.CUBE)).isZero();
        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The first ability only targets a creature you control")
    void firstAbilityOnlyTargetsCreatureYouControl() {
        harness.addToBattlefield(player1, new DelifsCube());
        Permanent opponentCreature = addCreatureReady(player2, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Activating after blockers are declared does not create the delayed trigger")
    void activatingAfterBlockersAreDeclaredDoesNotTrigger() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new DelifsCube());
        Permanent attacker = addCreatureReady(player1, new IcatianPhalanx());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(cube.getCounterCount(CounterType.CUBE)).isZero();
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The delayed trigger still adds a counter when the watched attacker leaves")
    void delayedTriggerStillAddsCounterWhenWatchedAttackerLeaves() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new DelifsCube());
        Permanent attacker = addCreatureReady(player1, new IcatianPhalanx());
        addCreatureReady(player2, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(harness.getPermanentRemovalService().removePermanentToGraveyard(gd, attacker)).isTrue();
        harness.passBothPriorities();

        assertThat(cube.getCounterCount(CounterType.CUBE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration requires a cube counter as an activation cost")
    void regenerationCannotBeActivatedWithoutCubeCounter() {
        harness.addToBattlefield(player1, new DelifsCube());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
        assertThat(target.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("A tapped Cube can regenerate and pays its counter before resolution")
    void tappedCubePaysRegenerationCounterImmediately() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new DelifsCube());
        cube.tap();
        cube.setCounterCount(CounterType.CUBE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(cube.getCounterCount(CounterType.CUBE)).isZero();
        assertThat(target.getRegenerationShield()).isZero();
        harness.passBothPriorities();
        assertThat(target.getRegenerationShield()).isEqualTo(1);
        assertThat(cube.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A returned Cube is not the source of the old delayed trigger")
    void returnedCubeDoesNotReceiveOldTriggerCounter() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new DelifsCube());
        Permanent attacker = addCreatureReady(player1, new IcatianPhalanx());
        addCreatureReady(player2, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(harness.getPermanentRemovalService().removePermanentToGraveyard(gd, cube)).isTrue();
        gd.playerGraveyards.get(player1.getId()).remove(cube.getCard());
        Permanent returnedCube = harness.addToBattlefieldAndReturn(player1, cube.getCard());
        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(returnedCube.getCounterCount(CounterType.CUBE)).isZero();
        resolveCombat();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The delayed trigger follows the creature after a control change")
    void delayedTriggerFollowsCreatureControlledByOpponent() {
        harness.forceActivePlayer(player2);
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new DelifsCube());
        Permanent attacker = addCreatureReady(player1, new IcatianPhalanx());
        addCreatureReady(player1, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerBattlefields.get(player2.getId()).add(attacker);
        attacker.setSummoningSick(false);
        declareAttackersAndPrepareBlockers(player2,
                List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(cube.getCounterCount(CounterType.CUBE)).isEqualTo(1);
        resolveCombat(player2);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Assigning no combat damage still applies when damage cannot be prevented")
    void assignsNoCombatDamageEvenWhenDamageCannotBePrevented() {
        Permanent cube = harness.addToBattlefieldAndReturn(player1, new DelifsCube());
        Permanent attacker = addCreatureReady(player1, new IcatianPhalanx());
        harness.addToBattlefield(player1, new QuestingBeast());
        addCreatureReady(player2, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        assertThat(cube.getCounterCount(CounterType.CUBE)).isEqualTo(1);
        resolveCombat();
        harness.assertLife(player2, 20);
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, assignments);
        harness.passBothPriorities();
    }
}
