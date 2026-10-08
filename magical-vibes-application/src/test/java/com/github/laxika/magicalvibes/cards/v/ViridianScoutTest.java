package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AuriokWindwalker;
import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.cards.f.FurnaceWhelp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViridianScout.class, AuriokWindwalker.class, FurnaceWhelp.class, DrossCrocodile.class})
class ViridianScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 2 damage to a target creature with flying")
    void sacrificesItselfAndDamagesFlyer() {
        harness.addToBattlefield(player1, new ViridianScout());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AuriokWindwalker());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Viridian Scout");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifices itself as a cost before the damage resolves")
    void sacrificesItselfBeforeDamageResolves() {
        harness.addToBattlefield(player1, new ViridianScout());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AuriokWindwalker());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Viridian Scout");
        assertThat(target.getMarkedDamage()).isEqualTo(0);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("2 damage kills a small flying creature")
    void killsSmallFlyer() {
        harness.addToBattlefield(player1, new ViridianScout());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FurnaceWhelp());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Viridian Scout");
        harness.assertInGraveyard(player2, "Furnace Whelp");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        harness.addToBattlefield(player1, new ViridianScout());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped summoning-sick Scout can damage its controller's flyer")
    void canActivateWhileTappedAndSummoningSickTargetingOwnFlyer() {
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new ViridianScout());
        scout.tap();
        scout.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AuriokWindwalker());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Viridian Scout");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without the required green mana")
    void cannotActivateWithoutGreenMana() {
        harness.addToBattlefield(player1, new ViridianScout());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AuriokWindwalker());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Viridian Scout");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
