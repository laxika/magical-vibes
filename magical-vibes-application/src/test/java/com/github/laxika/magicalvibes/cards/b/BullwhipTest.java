package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EnsnaringBridge;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Bullwhip.class, SpinedWurm.class, EnsnaringBridge.class})
class BullwhipTest extends BaseCardTest {

    @Test
    @DisplayName("Requires two generic mana to activate")
    void requiresTwoGenericMana() {
        Permanent bullwhip = harness.addToBattlefieldAndReturn(player1, new Bullwhip());
        Permanent target = addCreatureReady(player2, new SpinedWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bullwhip.isTapped()).isFalse();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Deals 1 damage to the target creature and forces it to attack")
    void damagesAndForcesTargetToAttack() {
        Permanent bullwhip = harness.addToBattlefieldAndReturn(player1, new Bullwhip());
        Permanent target = addCreatureReady(player2, new SpinedWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(bullwhip.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isMustAttackThisTurn()).isTrue();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("The must-attack requirement expires at the end of the turn")
    void mustAttackRequirementExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new Bullwhip());
        Permanent target = addCreatureReady(player2, new SpinedWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Does not require the target to attack when it cannot attack")
    void doesNotRequireAttackWhenUnable() {
        harness.addToBattlefield(player1, new Bullwhip());
        Permanent target = addCreatureReady(player2, new SpinedWurm());
        harness.addToBattlefield(player2, new EnsnaringBridge());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThatCode(() -> declareAttackers(player2, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Bullwhip());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new EnsnaringBridge());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Fizzles if the target creature leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent bullwhip = harness.addToBattlefieldAndReturn(player1, new Bullwhip());
        Permanent target = addCreatureReady(player2, new SpinedWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(bullwhip.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.isMustAttackThisTurn()).isFalse();
    }
}
