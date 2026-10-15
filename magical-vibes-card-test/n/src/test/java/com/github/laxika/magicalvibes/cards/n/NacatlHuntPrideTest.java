package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NacatlHuntPride.class, GrizzlyBears.class, Forest.class})
class NacatlHuntPrideTest extends BaseCardTest {

    private static final int ABILITY_CANT_BLOCK = 0;
    private static final int ABILITY_MUST_BLOCK = 1;

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

    @Test
    @DisplayName("Red ability prevents an otherwise legal block and taps its source")
    void redAbilityRejectsBlock() {
        Permanent source = addCreatureReady(player1, new NacatlHuntPride());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, ABILITY_CANT_BLOCK, null, target.getId());
        assertThat(source.isTapped()).isTrue();
        harness.passBothPriorities();
        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A can't-block restriction overrides the green ability's blocking requirement")
    void cannotBlockOverridesMustBlock() {
        addCreatureReady(player1, new NacatlHuntPride());
        addCreatureReady(player1, new NacatlHuntPride());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, ABILITY_MUST_BLOCK, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, ABILITY_CANT_BLOCK, null, target.getId());
        harness.passBothPriorities();
        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Both blocking effects expire through the engine's end-of-turn cleanup")
    void blockingEffectsExpireAtCleanup() {
        addCreatureReady(player1, new NacatlHuntPride());
        addCreatureReady(player1, new NacatlHuntPride());
        Permanent redTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent greenTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, ABILITY_CANT_BLOCK, null, redTarget.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, ABILITY_MUST_BLOCK, null, greenTarget.getId());
        harness.passBothPriorities();
        assertThat(redTarget.isCantBlockThisTurn()).isTrue();
        assertThat(greenTarget.isMustBlockThisTurnIfAble()).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(redTarget.isCantBlockThisTurn()).isFalse();
        assertThat(greenTarget.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    @DisplayName("Green ability can target its own source and pays the tap cost")
    void greenAbilityCanTargetItsSource() {
        Permanent source = addCreatureReady(player1, new NacatlHuntPride());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, ABILITY_MUST_BLOCK, null, source.getId());
        assertThat(source.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(source.isMustBlockThisTurnIfAble()).isTrue();
    }

    @Test
    @DisplayName("Neither ability can be activated while the source is tapped")
    void tappedSourceCannotActivateEitherAbility() {
        Permanent source = addCreatureReady(player1, new NacatlHuntPride());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        source.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        for (int abilityIndex : List.of(ABILITY_CANT_BLOCK, ABILITY_MUST_BLOCK)) {
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents activation of either tap ability")
    void summoningSickSourceCannotActivateEitherAbility() {
        harness.addToBattlefield(player1, new NacatlHuntPride());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        for (int abilityIndex : List.of(ABILITY_CANT_BLOCK, ABILITY_MUST_BLOCK)) {
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThat(gd.stack).isEmpty();
    }

    private void beginCombat(Permanent attacker) {
        attacker.setAttacking(true);
        prepareDeclareBlockers();
    }
}
