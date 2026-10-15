package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LostJitte.class, GrizzlyBears.class, Island.class, Boomerang.class})
class LostJitteTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature dealing combat damage puts a charge counter on Lost Jitte")
    void combatDamageAddsChargeCounter() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        jitte.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The first mode untaps a target land and removes one charge counter")
    void untapLandMode() {
        Permanent jitte = addJitteReady(player1);
        jitte.setCounterCount(CounterType.CHARGE, 1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        land.tap();

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The untap mode cannot target a creature")
    void untapModeRequiresLandTarget() {
        Permanent jitte = addJitteReady(player1);
        jitte.setCounterCount(CounterType.CHARGE, 1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second mode makes a target creature unable to block this turn")
    void cantBlockMode() {
        Permanent jitte = addJitteReady(player1);
        jitte.setCounterCount(CounterType.CHARGE, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction wears off at end of turn")
    void cantBlockModeWearsOffAtEndOfTurn() {
        Permanent jitte = addJitteReady(player1);
        jitte.setCounterCount(CounterType.CHARGE, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The third mode puts a +1/+1 counter on the equipped creature")
    void equippedCreatureCounterMode() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        jitte.setAttachedTo(creature.getId());
        jitte.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip attaches Lost Jitte to a creature you control")
    void equipAttaches() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 3, null, creature.getId());
        harness.passBothPriorities();

        assertThat(jitte.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addJitteReady(Player player) {
        Permanent jitte = harness.addToBattlefieldAndReturn(player, new LostJitte());
        jitte.setSummoningSick(false);
        return jitte;
    }

    @Test
    @DisplayName("Combat damage to a blocker triggers even when the equipped creature dies")
    void combatDamageToBlockerWithLethalReturnDamage() {
        Permanent jitte = addJitteReady(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        jitte.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(jitte.getAttachedTo()).isNull();
        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The equipment controller gets the trigger when another player controls the creature")
    void differentlyControlledEquipmentTriggers() {
        Permanent jitte = addJitteReady(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        jitte.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The untap mode can target an opponent's land while Lost Jitte is unequipped")
    void untapOpponentsLandWhileUnequipped() {
        Permanent jitte = addJitteReady(player1);
        jitte.setCounterCount(CounterType.CHARGE, 2);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        land.tap();

        harness.activateAbility(player1, 0, 0, null, land.getId());
        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already untapped land is a legal target and the charge is still spent")
    void untapModeAcceptsUntappedLand() {
        Permanent jitte = addJitteReady(player1);
        jitte.setCounterCount(CounterType.CHARGE, 1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());

        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("The can't-block mode cannot target a land")
    void cantBlockModeRejectsLand() {
        Permanent jitte = addJitteReady(player1);
        jitte.setCounterCount(CounterType.CHARGE, 1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("None of the three modes can be activated without a charge counter")
    void allModesRequireChargeCounter() {
        Permanent jitte = addJitteReady(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        jitte.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The counter mode can be activated while unequipped and spends its charge")
    void counterModeWhileUnequippedDoesNothing() {
        Permanent jitte = addJitteReady(player1);
        jitte.setCounterCount(CounterType.CHARGE, 1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Returning the equipped creature in response prevents the counter placement")
    void counterModeDoesNothingAfterCreatureLeaves() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        jitte.setAttachedTo(creature.getId());
        jitte.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(jitte.getAttachedTo()).isNull();
        assertThat(jitte.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter mode uses the last attachment when Lost Jitte leaves in response")
    void counterModeResolvesAfterEquipmentLeaves() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        jitte.setAttachedTo(creature.getId());
        jitte.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.castInstant(player2, 0, jitte.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(jitte);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentsCreature() {
        Permanent jitte = addJitteReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(jitte.getAttachedTo()).isNull();
    }
}
