package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
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

class NacatlHuntPrideTest extends BaseCardTest {

    private static final int ABILITY_CANT_BLOCK = 0;
    private static final int ABILITY_MUST_BLOCK = 1;

    // ===== {R}, {T}: Target creature can't block this turn. =====

    @Test
    @DisplayName("Red ability makes the target creature unable to block this turn")
    void redAbilityPreventsBlocking() {
        addCreatureReady(player1, new NacatlHuntPride());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, ABILITY_CANT_BLOCK, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    // ===== {G}, {T}: Target creature blocks this turn if able. =====

    @Test
    @DisplayName("Green ability forces the target to be declared as a blocker when able")
    void greenAbilityForcesBlock() {
        addCreatureReady(player1, new NacatlHuntPride());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, ABILITY_MUST_BLOCK, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustBlockThisTurnIfAble()).isTrue();

        beginCombat(attacker);

        // Declaring no blockers is illegal — the target must block if able.
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Green ability requirement is satisfied by declaring the block")
    void greenAbilitySatisfiedByBlocking() {
        addCreatureReady(player1, new NacatlHuntPride());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, ABILITY_MUST_BLOCK, null, target.getId());
        harness.passBothPriorities();

        beginCombat(attacker);

        // Blocker (defender index 0) blocks the attacker (attacker index 1) — requirement met.
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Green ability imposes no requirement when the target can't legally block (tapped)")
    void greenAbilityNoRequirementWhenUnable() {
        addCreatureReady(player1, new NacatlHuntPride());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, ABILITY_MUST_BLOCK, null, target.getId());
        harness.passBothPriorities();

        target.tap();
        beginCombat(attacker);

        // Tapped creature is unable to block, so declaring no blockers is legal.
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Green ability requirement wears off at end of turn")
    void greenAbilityWearsOff() {
        addCreatureReady(player1, new NacatlHuntPride());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, ABILITY_MUST_BLOCK, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isMustBlockThisTurnIfAble()).isTrue();

        // End-of-turn cleanup clears "until end of turn" combat requirements.
        target.resetModifiers();
        assertThat(target.isMustBlockThisTurnIfAble()).isFalse();

        beginCombat(attacker);

        // Requirement gone — declaring no blockers is legal again.
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Abilities can't target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new NacatlHuntPride());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, ABILITY_MUST_BLOCK, null, land.getId()))
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
