package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MystifyingMaze.class, LlanowarElves.class, MindControl.class})
class MystifyingMazeTest extends BaseCardTest {

    @Test
    void tapAddsColorlessManaWithoutUsingTheStack() {
        var maze = harness.addToBattlefieldAndReturn(player1, new MystifyingMaze());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(maze.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilesOpponentAttackerAndReturnsTappedAtTheirEndStep() {
        var attacker = prepareOpponentAttack();
        var maze = findPermanent(player1, "Mystifying Maze");

        activateExile(attacker);

        assertThat(maze.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(attacker.getCard());

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        var returned = findPermanent(player2, "Llanowar Elves");
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttacking()).isFalse();
        assertThat(returned.getId()).isNotEqualTo(attacker.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(attacker.getCard());
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotTargetOpponentNonAttacker() {
        harness.addToBattlefield(player1, new MystifyingMaze());
        var creature = addCreatureReady(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOwnAttacker() {
        harness.addToBattlefield(player1, new MystifyingMaze());
        var creature = addCreatureReady(player1, new LlanowarElves());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateExileWithOnlyThreeMana() {
        var attacker = prepareOpponentAttack();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Mystifying Maze").isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    void abilityFizzlesIfTargetLeavesBeforeResolution() {
        var attacker = prepareOpponentAttack();
        activateExile(attacker);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, attacker));

        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.END_STEP);

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void delayedReturnSurvivesMazeLeavingBattlefield() {
        var attacker = prepareOpponentAttack();
        var maze = findPermanent(player1, "Mystifying Maze");
        activateExile(attacker);
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, maze));

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Llanowar Elves").isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Mystifying Maze");
    }

    @Test
    void stolenAttackerReturnsUnderOwnersControlAndLosesItsAura() {
        harness.addToBattlefield(player1, new MystifyingMaze());
        var creature = addCreatureReady(player1, new LlanowarElves());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new MindControl()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castEnchantment(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.performUntapStep(player2);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(attackerIndex)));
        activateExile(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mind Control");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature.getCard());
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Llanowar Elves").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    private Permanent prepareOpponentAttack() {
        harness.addToBattlefield(player1, new MystifyingMaze());
        var attacker = addCreatureReady(player2, new LlanowarElves());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));
        return attacker;
    }

    private void activateExile(Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 1, null, target.getId());
    }
}
