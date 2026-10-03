package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.v.ViridianLongbow;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BansheesBlade.class, YotianSoldier.class})
class BansheesBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each charge counter")
    void equippedCreatureBoostedPerChargeCounter() {
        Permanent creature = addCreatureReady(player1, new YotianSoldier());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BansheesBlade());
        blade.setCounterCount(CounterType.CHARGE, 3);
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }

    @Test
    @DisplayName("Equipped creature dealing combat damage puts a charge counter on the Blade")
    void combatDamageAddsChargeCounter() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BansheesBlade());
        Permanent creature = addCreatureReady(player1, new YotianSoldier());
        blade.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(blade.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equipped creature dealing combat damage to a creature puts a charge counter on the Blade")
    void combatDamageToCreatureAddsChargeCounter() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BansheesBlade());
        Permanent attacker = addCreatureReady(player1, new YotianSoldier());
        addCreatureReady(player2, new YotianSoldier());
        blade.setAttachedTo(attacker.getId());

        declareAttackersAndPrepareBlockers(player1, List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(blade.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip attaches the Blade to a creature you control")
    void equipAttaches() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BansheesBlade());
        Permanent creature = addCreatureReady(player1, new YotianSoldier());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentCreature() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BansheesBlade());
        Permanent opponentCreature = addCreatureReady(player2, new YotianSoldier());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    void reEquippingTransfersBonusAndPreservesChargeCounters() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BansheesBlade());
        Permanent first = addCreatureReady(player1, new YotianSoldier());
        Permanent second = addCreatureReady(player1, new YotianSoldier());
        blade.setCounterCount(CounterType.CHARGE, 2);
        blade.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(second.getId());
        assertThat(blade.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @CardUsed(ViridianLongbow.class)
    void noncombatDamageDoesNotAddChargeCounter() {
        Permanent creature = addCreatureReady(player1, new YotianSoldier());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BansheesBlade());
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        blade.setAttachedTo(creature.getId());
        longbow.setAttachedTo(creature.getId());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(blade.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BansheesBlade());
        Permanent creature = addCreatureReady(player1, new YotianSoldier());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    void onlyChargeCountersOnBladeContributeToBonus() {
        Permanent creature = addCreatureReady(player1, new YotianSoldier());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BansheesBlade());
        blade.setAttachedTo(creature.getId());
        creature.setCounterCount(CounterType.CHARGE, 3);
        blade.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        blade.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }
}
