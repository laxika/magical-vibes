package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BlazingArchon;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlluringSiren.class, RuneclawBear.class})
class AlluringSirenTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack targeting an opponent's creature")
    void activatingPutsOnStack() {
        addReadySiren(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving ability marks target creature as must attack this turn")
    void resolvingMarksMustAttack() {
        addReadySiren(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot target own creature")
    void cannotTargetOwnCreature() {
        addReadySiren(player1);
        Permanent ownCreature = addCreatureReady(player1, new RuneclawBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires tap so cannot activate twice")
    void requiresTapCannotActivateTwice() {
        addReadySiren(player1);
        Permanent target1 = addCreatureReady(player2, new RuneclawBear());
        Permanent target2 = addCreatureReady(player2, new RuneclawBear());

        harness.activateAbility(player1, 0, null, target1.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadySiren(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Resolving ability sets mustAttackTargetId to the Siren controller")
    void resolvingSetsAttackTargetToController() {
        addReadySiren(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(target.getMustAttackTargetId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Must attack flag and target are cleared at end of turn")
    void mustAttackClearedAtEndOfTurn() {
        addReadySiren(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(target.getMustAttackTargetId()).isEqualTo(player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isMustAttackThisTurn()).isFalse();
        assertThat(target.getMustAttackTargetId()).isNull();
    }

    @Test
    void readyTargetCannotStayOutOfCombat() {
        addReadySiren(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void readyTargetCanAttackSirenController() {
        addReadySiren(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));

        assertThat(target.isAttacking()).isTrue();
        assertThat(target.getAttackTarget()).isEqualTo(player1.getId());
    }

    @Test
    void tappedTargetIsNotRequiredToAttack() {
        addReadySiren(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        target.tap();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        declareAttackers(player2, List.of());

        assertThat(target.isAttackedThisTurn()).isFalse();
    }

    @Test
    void summoningSickTargetIsNotRequiredToAttack() {
        addReadySiren(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        declareAttackers(player2, List.of());

        assertThat(target.isAttackedThisTurn()).isFalse();
    }

    @Test
    void summoningSickSirenCannotPayTapCost() {
        harness.addToBattlefield(player1, new AlluringSiren());
        Permanent target = addCreatureReady(player2, new RuneclawBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetBecomingControlledByAbilityControllerMakesAbilityFizzle() {
        addReadySiren(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.isMustAttackThisTurn()).isFalse();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    void abilityStillResolvesAfterSirenLeavesBattlefield() {
        Permanent siren = addReadySiren(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(siren);

        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(target.getMustAttackTargetId()).isEqualTo(player1.getId());
        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @CardUsed(JaceBeleren.class)
    void cannotTargetOpponentsPlaneswalker() {
        addReadySiren(player1);
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, jace.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(JaceBeleren.class)
    void cannotAttackPlaneswalkerWhenSirenControllerCanBeAttacked() {
        addReadySiren(player1);
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        prepareAttackDeclaration(player2);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0), Map.of(0, jace.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack the specified player");
    }

    @Test
    @CardUsed({BlazingArchon.class, JaceBeleren.class})
    void mayAttackPlaneswalkerWhenSirenControllerCannotBeAttacked() {
        addReadySiren(player1);
        harness.addToBattlefield(player1, new BlazingArchon());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        prepareAttackDeclaration(player2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player2, List.of(0), Map.of(0, jace.getId())));

        assertThat(target.isAttacking()).isTrue();
        assertThat(target.getAttackTarget()).isEqualTo(jace.getId());
    }

    @Test
    @CardUsed({BlazingArchon.class, JaceBeleren.class})
    void mayDeclineToAttackWhenOnlyPlaneswalkerCanBeAttacked() {
        addReadySiren(player1);
        harness.addToBattlefield(player1, new BlazingArchon());
        harness.addToBattlefield(player1, new JaceBeleren());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        declareAttackers(player2, List.of());

        assertThat(target.isAttackedThisTurn()).isFalse();
    }

    private void prepareAttackDeclaration(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private Permanent addReadySiren(Player player) {
        return addCreatureReady(player, new AlluringSiren());
    }
}
