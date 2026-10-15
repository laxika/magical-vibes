package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilentDart.class, GrizzlyBears.class, ColossalDreadmaw.class})
class SilentDartTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 3 damage to target creature")
    void sacrificesItselfAndDealsDamageToTargetCreature() {
        harness.addToBattlefield(player1, new SilentDart());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Silent Dart");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new SilentDart());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dealsExactlyThreeDamageToOwnCreatureAfterSacrifice() {
        harness.addToBattlefield(player1, new SilentDart());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Silent Dart");
        assertThat(target.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Colossal Dreadmaw");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent dart = harness.addToBattlefieldAndReturn(player1, new SilentDart());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        dart.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player1, "Silent Dart");
        harness.assertNotInGraveyard(player1, "Silent Dart");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithOnlyThreeMana() {
        Permanent dart = harness.addToBattlefieldAndReturn(player1, new SilentDart());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertOnBattlefield(player1, "Silent Dart");
        harness.assertNotInGraveyard(player1, "Silent Dart");
        assertThat(dart.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
