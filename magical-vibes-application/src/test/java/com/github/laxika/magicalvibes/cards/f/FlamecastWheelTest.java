package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlamecastWheel.class, AirElemental.class, BronzeSable.class})
class FlamecastWheelTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 3 damage to target creature")
    void sacrificesItselfAndDealsDamage() {
        harness.addToBattlefield(player1, new FlamecastWheel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotOnBattlefield(player1, "Flamecast Wheel");
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new FlamecastWheel());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Flamecast Wheel");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new FlamecastWheel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        wheel.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Flamecast Wheel");
        harness.assertNotInGraveyard(player1, "Flamecast Wheel");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without five mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new FlamecastWheel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(wheel.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Flamecast Wheel");
        harness.assertNotInGraveyard(player1, "Flamecast Wheel");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target your own creature and deal lethal damage after the wheel is sacrificed")
    void canDealLethalDamageToOwnCreature() {
        harness.addToBattlefield(player1, new FlamecastWheel());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Flamecast Wheel");
        harness.assertOnBattlefield(player1, "Bronze Sable");
        assertThat(target.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bronze Sable");
        harness.assertInGraveyard(player1, "Bronze Sable");
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        harness.addToBattlefield(player1, new FlamecastWheel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlamecastWheel());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Flamecast Wheel");
        harness.assertNotInGraveyard(player1, "Flamecast Wheel");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }
}
