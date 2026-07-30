package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourtlyProvocateurTest extends BaseCardTest {

    private static final int ABILITY_MUST_ATTACK = 0;
    private static final int ABILITY_MUST_BLOCK = 1;

    @Test
    @DisplayName("First ability forces the target to attack this turn without dictating whom it attacks")
    void mustAttackAbility() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, ABILITY_MUST_ATTACK, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(target.getMustAttackTargetId()).isNull();
    }

    @Test
    @DisplayName("First ability can target a creature its controller controls")
    void mustAttackCanTargetOwnCreature() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, ABILITY_MUST_ATTACK, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Must-attack requirement wears off at end of turn")
    void mustAttackWearsOff() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, ABILITY_MUST_ATTACK, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isMustAttackThisTurn()).isTrue();

        target.resetModifiers();
        assertThat(target.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Second ability forces the target to be declared as a blocker when able")
    void mustBlockAbility() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, ABILITY_MUST_BLOCK, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustBlockThisTurnIfAble()).isTrue();

        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Second ability requirement is satisfied by declaring the block")
    void mustBlockSatisfiedByBlocking() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, ABILITY_MUST_BLOCK, null, target.getId());
        harness.passBothPriorities();

        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Abilities can't target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, ABILITY_MUST_ATTACK, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void beginCombat(Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
