package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BrothersOfFire;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MazeOfIth.class, Squire.class, BrothersOfFire.class})
class MazeOfIthTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps the target attacking creature")
    void untapsTargetAttacker() {
        Permanent maze = addMaze();
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        attacker.tap();

        activateMaze(maze, attacker);

        assertThat(attacker.isTapped()).isFalse();
        assertThat(maze.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Prevents combat damage dealt by the target creature")
    void preventsCombatDamageDealtByTarget() {
        harness.setLife(player2, 20);
        Permanent maze = addMaze();
        Permanent attacker = addAttacker(player1, player2, 2, 2);

        activateMaze(maze, attacker);
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotPreventCombatDamageDealtByOtherCreatures() {
        harness.setLife(player2, 20);
        Permanent maze = addMaze();
        Permanent protectedAttacker = addAttacker(player1, player2, 2, 2);
        addAttacker(player1, player2, 2, 2);

        activateMaze(maze, protectedAttacker);
        resolveCombat();

        harness.assertLife(player2, 18);
    }
    @Test
    @DisplayName("Prevents combat damage dealt to the target creature")
    void preventsCombatDamageDealtToTarget() {
        Permanent maze = addMaze();
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        addCreatureReady(player2, new BrothersOfFire());

        activateMazeAndStopAtBlockers(maze, attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    void doesNotPreventCombatDamageDealtToOtherCreatures() {
        Permanent maze = addMaze();
        Permanent protectedAttacker = addAttacker(player1, player2, 2, 2);
        Permanent otherAttacker = addAttacker(player1, player2, 2, 2);
        addCreatureReady(player2, new BrothersOfFire());

        activateMazeAndStopAtBlockers(maze, protectedAttacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 2)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedAttacker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherAttacker);
    }
    @Test
    @DisplayName("Does not prevent noncombat damage to the target creature")
    void doesNotPreventNoncombatDamage() {
        Permanent maze = addMaze();
        Permanent attacker = addAttacker(player1, player2, 3, 3);

        activateMaze(maze, attacker);

        Permanent brothers = addCreatureReady(player2, new BrothersOfFire());
        activateBrothersOfFire(brothers, attacker);

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttacker() {
        Permanent maze = addMaze();
        Permanent bystander = addCreatureReady(player1, new Squire());
        prepareActivation();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(maze);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target an attacking creature controlled by another player")
    void canTargetOpponentsAttacker() {
        Permanent maze = addMaze();
        Permanent attacker = addAttacker(player2, player1, 2, 2);
        attacker.tap();

        activateMaze(maze, attacker);

        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    void untappedAttackerRemainsInCombatAndDealsNoCombatDamage() {
        harness.setLife(player2, 20);
        Permanent maze = addMaze();
        Permanent attacker = addCreatureReady(player1, new BrothersOfFire());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        activateMaze(maze, attacker);

        assertThat(attacker.isTapped()).isFalse();
        assertThat(attacker.isAttacking()).isTrue();
        resolveCombat();
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotUntapTargetThatStopsAttackingBeforeResolution() {
        Permanent maze = addMaze();
        Permanent attacker = addCreatureReady(player1, new BrothersOfFire());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        attacker.tap();
        prepareActivation();
        harness.activateAbility(player1, 0, null, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isTrue();
        assertThat(maze.isTapped()).isTrue();
    }

    @Test
    void doesNotPreventNoncombatDamageDealtByTarget() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent maze = addMaze();
        Permanent attacker = addCreatureReady(player1, new BrothersOfFire());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        activateMaze(maze, attacker);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    void canUntapAttackerAfterDamageWithoutUndoingDamage() {
        harness.setLife(player2, 20);
        Permanent maze = addMaze();
        Permanent attacker = addCreatureReady(player1, new BrothersOfFire());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        attacker.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.assertLife(player2, 18);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isFalse();
        harness.assertLife(player2, 18);
        assertThat(maze.isTapped()).isTrue();
    }

    private Permanent addMaze() {
        return harness.addToBattlefieldAndReturn(player1, new MazeOfIth());
    }

    private void activateMaze(Permanent maze, Permanent target) {
        prepareActivation();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(maze);
        harness.activateAbility(player1, index, null, target.getId());
        harness.passBothPriorities();
    }

    private void activateMazeAndStopAtBlockers(Permanent maze, Permanent target) {
        prepareActivation();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(maze);
        harness.activateAbility(player1, index, null, target.getId());
        harness.passUntil(TurnStep.DECLARE_BLOCKERS);
    }

    private void activateBrothersOfFire(Permanent brothers, Permanent target) {
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 2);
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(brothers);
        harness.activateAbility(player2, index, null, target.getId());
        harness.passBothPriorities();
    }

    private void prepareActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
    }

    private Permanent addAttacker(Player owner, Player defender, int power, int toughness) {
        Card creature = new Squire();
        creature.setPower(power);
        creature.setToughness(toughness);
        Permanent attacker = addCreatureReady(owner, creature);
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        return attacker;
    }

}
