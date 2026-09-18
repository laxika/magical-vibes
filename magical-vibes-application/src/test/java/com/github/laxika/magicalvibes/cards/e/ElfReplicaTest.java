package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SphereOfPurity;
import com.github.laxika.magicalvibes.cards.s.SteelWall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElfReplica.class, SphereOfPurity.class, SteelWall.class})
class ElfReplicaTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability destroys target enchantment")
    void sacrificeAbilityDestroysTargetEnchantment() {
        addElfReplica();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SphereOfPurity());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elf Replica");
        harness.assertInGraveyard(player2, "Sphere of Purity");
    }

    @Test
    @DisplayName("Sacrifice ability can destroy an enchantment its controller controls")
    void sacrificeAbilityDestroysOwnEnchantment() {
        addElfReplica();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SphereOfPurity());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elf Replica");
        harness.assertInGraveyard(player1, "Sphere of Purity");
    }

    @Test
    @DisplayName("Cannot target a non-enchantment permanent")
    void cannotTargetNonEnchantmentPermanent() {
        addElfReplica();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SteelWall());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid when the ability is activated")
    void sacrificeIsPaidOnActivation() {
        addElfReplica();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SphereOfPurity());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Elf Replica");
        harness.assertOnBattlefield(player2, "Sphere of Purity");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability fizzles if the target enchantment leaves before resolution")
    void fizzlesIfTargetEnchantmentLeavesBeforeResolution() {
        addElfReplica();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SphereOfPurity());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elf Replica");
        harness.assertInGraveyard(player2, "Sphere of Purity");
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addElfReplica();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SphereOfPurity());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addElfReplica() {
        return harness.addToBattlefieldAndReturn(player1, new ElfReplica());
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
