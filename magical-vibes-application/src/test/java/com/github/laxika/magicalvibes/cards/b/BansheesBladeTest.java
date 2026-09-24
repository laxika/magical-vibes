package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
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
}
