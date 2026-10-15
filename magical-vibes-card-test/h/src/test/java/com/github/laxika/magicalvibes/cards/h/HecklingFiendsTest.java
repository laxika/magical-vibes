package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.ExecutionersHood;
import com.github.laxika.magicalvibes.cards.r.RussetWolves;
import com.github.laxika.magicalvibes.model.ManaColor;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HecklingFiends.class, RussetWolves.class, ExecutionersHood.class})
class HecklingFiendsTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingPutsAbilityOnStack() {
        addReadyFiends(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving ability marks target creature as must attack without a specific player target")
    void resolvingMarksTargetMustAttack() {
        addReadyFiends(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(target.getMustAttackTargetId()).isNull();
    }

    @Test
    @DisplayName("Can target its controller's own creature")
    void canTargetOwnCreature() {
        addReadyFiends(player1);
        Permanent target = addReadyCreature(player1);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(target.getMustAttackTargetId()).isNull();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyFiends(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ExecutionersHood());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not tap to activate")
    void doesNotTapToActivate() {
        Permanent fiends = addReadyFiends(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(fiends.isTapped()).isFalse();
    }

    @Test
    void affectedCreatureMustAttackWhenAble() {
        addReadyFiends(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThat(target.isAttacking()).isTrue();
    }

    @Test
    void tappedTargetIsNotForcedToAttack() {
        addReadyFiends(player1);
        Permanent target = addReadyCreature(player2);
        target.tap();
        addActivationMana();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        declareAttackers(player2, List.of());

        assertThat(target.isAttacking()).isFalse();
    }

    @Test
    void summoningSickTargetIsNotForcedToAttack() {
        addReadyFiends(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RussetWolves());
        addActivationMana();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        declareAttackers(player2, List.of());

        assertThat(target.isAttacking()).isFalse();
    }

    @Test
    void requirementExpiresAtCleanup() {
        addReadyFiends(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isMustAttackThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(target.isMustAttackThisTurn()).isFalse();
        declareAttackers(player2, List.of());
        assertThat(target.isAttacking()).isFalse();
    }

    @Test
    void tappedSummoningSickFiendsCanActivateTwice() {
        Permanent fiends = harness.addToBattlefieldAndReturn(player1, new HecklingFiends());
        fiends.tap();
        Permanent first = addReadyCreature(player2);
        Permanent second = addReadyCreature(player2);
        addActivationMana();
        addActivationMana();

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(first.isMustAttackThisTurn()).isTrue();
        assertThat(second.isMustAttackThisTurn()).isTrue();
        assertThat(fiends.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutRedMana() {
        addReadyFiends(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(target.isMustAttackThisTurn()).isFalse();
    }

    @Test
    void targetLeavingBattlefieldDoesNotAffectAnotherCreature() {
        addReadyFiends(player1);
        Permanent target = addReadyCreature(player2);
        Permanent other = addReadyCreature(player2);
        addActivationMana();
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(other.isMustAttackThisTurn()).isFalse();
    }

    private Permanent addReadyFiends(Player player) {
        return addCreatureReady(player, new HecklingFiends());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new RussetWolves());
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
