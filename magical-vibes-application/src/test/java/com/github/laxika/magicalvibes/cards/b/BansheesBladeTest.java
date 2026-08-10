package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BansheesBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each charge counter")
    void equippedCreatureBoostedPerChargeCounter() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setCounterCount(CounterType.CHARGE, 3);
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equipped creature dealing combat damage puts a charge counter on the Blade")
    void combatDamageAddsChargeCounter() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        blade.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(blade.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip attaches the Blade to a creature you control")
    void equipAttaches() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentCreature() {
        Permanent blade = addBladeReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blade.getAttachedTo()).isNull();
    }

    private Permanent addBladeReady(Player player) {
        Permanent blade = new Permanent(new BansheesBlade());
        blade.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(blade);
        return blade;
    }
}
