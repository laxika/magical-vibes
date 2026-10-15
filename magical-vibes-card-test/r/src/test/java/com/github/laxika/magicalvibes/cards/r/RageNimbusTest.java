package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
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

@CardUsed({RageNimbus.class, GlorySeeker.class, PropheticPrism.class})
class RageNimbusTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingPutsAbilityOnStack() {
        addReadyNimbus(player1);
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
        addReadyNimbus(player1);
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
        addReadyNimbus(player1);
        Permanent target = addReadyCreature(player1);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyNimbus(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not tap to activate")
    void doesNotTapToActivate() {
        Permanent nimbus = addReadyNimbus(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(nimbus.isTapped()).isFalse();
    }

    @Test
    void targetMustAttackWhenAble() {
        addReadyNimbus(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void tappedTargetDoesNotHaveToAttack() {
        addReadyNimbus(player1);
        Permanent target = addReadyCreature(player2);
        target.tap();
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        declareAttackers(player2, List.of());

        assertThat(target.isAttacking()).isFalse();
    }

    @Test
    void summoningSickTargetDoesNotHaveToAttack() {
        addReadyNimbus(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        declareAttackers(player2, List.of());

        assertThat(target.isAttacking()).isFalse();
    }

    @Test
    void targetingNimbusDoesNotOverrideDefender() {
        Permanent nimbus = addReadyNimbus(player1);
        addActivationMana();

        harness.activateAbility(player1, 0, null, nimbus.getId());
        harness.passBothPriorities();
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        declareAttackers(List.of());

        assertThat(nimbus.isAttacking()).isFalse();
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent nimbus = harness.addToBattlefieldAndReturn(player1, new RageNimbus());
        nimbus.tap();
        Permanent target = addReadyCreature(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(nimbus.isTapped()).isTrue();
    }

    @Test
    void requirementExpiresAtEndOfTurn() {
        addReadyNimbus(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isMustAttackThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(target.isMustAttackThisTurn()).isFalse();
        declareAttackers(player2, List.of());
        assertThat(target.isAttacking()).isFalse();
    }

    @Test
    void removedTargetDoesNotAffectAnotherCreature() {
        addReadyNimbus(player1);
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

    @Test
    void cannotActivateWithoutRedMana() {
        addReadyNimbus(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void requirementSurvivesSourceLeavingBattlefield() {
        Permanent nimbus = addReadyNimbus(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(nimbus);
        gd.playerGraveyards.get(player1.getId()).add(nimbus.getCard());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    private Permanent addReadyNimbus(Player player) {
        return addCreatureReady(player, new RageNimbus());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new GlorySeeker());
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
