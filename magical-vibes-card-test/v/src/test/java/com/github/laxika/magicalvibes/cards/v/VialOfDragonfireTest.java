package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VialOfDragonfire.class, GrizzlyBears.class, CentaurCourser.class})
class VialOfDragonfireTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing the Vial deals 2 damage to target creature")
    void dealsDamageToTargetCreature() {
        harness.addToBattlefield(player1, new VialOfDragonfire());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vial of Dragonfire");
        harness.assertInGraveyard(player1, "Vial of Dragonfire");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new VialOfDragonfire());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Vial is sacrificed before resolution and deals exactly two damage to your own creature")
    void sacrificesAsCostAndDamagesOwnCreature() {
        harness.addToBattlefield(player1, new VialOfDragonfire());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CentaurCourser());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Vial of Dragonfire");
        harness.assertInGraveyard(player1, "Vial of Dragonfire");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Centaur Courser");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped Vial cannot pay its tap cost")
    void cannotActivateWhenTapped() {
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new VialOfDragonfire());
        vial.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CentaurCourser());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Vial of Dragonfire");
        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("One mana cannot pay the Vial's two-mana activation cost")
    void cannotActivateWithInsufficientMana() {
        harness.addToBattlefield(player1, new VialOfDragonfire());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CentaurCourser());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Vial of Dragonfire");
        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
    }
}
