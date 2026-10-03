package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurningTreeBloodscale.class, IzzetSignet.class})
class BurningTreeBloodscaleTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 1: enters with a +1/+1 counter after an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castBloodscale();

        assertThat(findPermanent(player1, "Burning-Tree Bloodscale")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst 1: enters without a counter when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castBloodscale();

        assertThat(findPermanent(player1, "Burning-Tree Bloodscale")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("Bloodthirst 1 ignores damage dealt to its controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 1);
        castBloodscale();

        assertThat(findPermanent(player1, "Burning-Tree Bloodscale")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("Bloodthirst 1 counts damage dealt after casting and before entering")
    void bloodthirstCountsDamageBeforeEntering() {
        harness.castFromHand(player1, new BurningTreeBloodscale(), "{2}{R}{G}");
        gd.recordDamageToPlayer(player2.getId(), 1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Burning-Tree Bloodscale")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Red ability prevents the target creature from blocking this creature")
    void redAbilityPreventsBlockingThisCreature() {
        Permanent source = addCreatureReady(player1, new BurningTreeBloodscale());
        Permanent blocker = addCreatureReady(player2, new BurningTreeBloodscale());
        addRedAbilityMana();

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        source.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Red ability still allows the target creature to block another creature")
    void redAbilityOnlyPreventsBlockingThisCreature() {
        Permanent source = addCreatureReady(player1, new BurningTreeBloodscale());
        Permanent otherAttacker = addCreatureReady(player1, new BurningTreeBloodscale());
        Permanent blocker = addCreatureReady(player2, new BurningTreeBloodscale());
        addRedAbilityMana();

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        source.setAttacking(true);
        otherAttacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Green ability requires the target creature to block this creature if able")
    void greenAbilityForcesBlockingThisCreature() {
        Permanent source = addCreatureReady(player1, new BurningTreeBloodscale());
        Permanent blocker = addCreatureReady(player2, new BurningTreeBloodscale());
        addGreenAbilityMana();

        harness.activateAbility(player1, 0, 1, null, blocker.getId());
        harness.passBothPriorities();

        source.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Green ability does not require blocking when the target creature cannot block")
    void greenAbilityAllowsNoBlockWhenTargetCannotBlock() {
        Permanent source = addCreatureReady(player1, new BurningTreeBloodscale());
        Permanent blocker = addCreatureReady(player2, new BurningTreeBloodscale());
        blocker.tap();
        addGreenAbilityMana();

        harness.activateAbility(player1, 0, 1, null, blocker.getId());
        harness.passBothPriorities();

        source.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Abilities can target only creatures")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new BurningTreeBloodscale());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());
        addGreenAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");

        addRedAbilityMana();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Bloodthirst adds only one counter even after multiple damage events")
    void bloodthirstDoesNotScaleWithDamage() {
        gd.recordDamageToPlayer(player2.getId(), 3);
        gd.recordDamageToPlayer(player2.getId(), 2);
        castBloodscale();

        assertThat(findPermanent(player1, "Burning-Tree Bloodscale")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The red blocking restriction overrides the green blocking requirement")
    void cannotBlockOverridesMustBlock() {
        Permanent source = addCreatureReady(player1, new BurningTreeBloodscale());
        Permanent blocker = addCreatureReady(player2, new BurningTreeBloodscale());
        addGreenAbilityMana();
        harness.activateAbility(player1, 0, 1, null, blocker.getId());
        harness.passBothPriorities();
        addRedAbilityMana();
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        source.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Green ability does not force blocking a different attacker when its source is not attacking")
    void greenAbilityDoesNotRequireBlockingOtherAttackers() {
        addCreatureReady(player1, new BurningTreeBloodscale());
        Permanent otherAttacker = addCreatureReady(player1, new BurningTreeBloodscale());
        Permanent blocker = addCreatureReady(player2, new BurningTreeBloodscale());
        addGreenAbilityMana();
        harness.activateAbility(player1, 0, 1, null, blocker.getId());
        harness.passBothPriorities();

        otherAttacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A creature required to block two Bloodscales may block either one")
    void competingGreenAbilitiesAllowEitherRequiredAttacker() {
        Permanent first = addCreatureReady(player1, new BurningTreeBloodscale());
        Permanent second = addCreatureReady(player1, new BurningTreeBloodscale());
        Permanent blocker = addCreatureReady(player2, new BurningTreeBloodscale());
        addGreenAbilityMana();
        harness.activateAbility(player1, 0, 1, null, blocker.getId());
        harness.passBothPriorities();
        addGreenAbilityMana();
        harness.activateAbility(player1, 1, 1, null, blocker.getId());
        harness.passBothPriorities();

        first.setAttacking(true);
        second.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .doesNotThrowAnyException();
    }

    private void castBloodscale() {
        harness.castFromHand(player1, new BurningTreeBloodscale(), "{2}{R}{G}");
        resolveAllTriggers();
    }

    private void addRedAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addGreenAbilityMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
