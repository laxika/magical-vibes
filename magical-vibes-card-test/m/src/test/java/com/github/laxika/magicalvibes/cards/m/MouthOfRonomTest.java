package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BorealCentaur;
import com.github.laxika.magicalvibes.cards.b.BorealShelf;
import com.github.laxika.magicalvibes.cards.p.PhyrexianSnowcrusher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MouthOfRonom.class, BorealCentaur.class, BorealShelf.class, PhyrexianSnowcrusher.class})
class MouthOfRonomTest extends BaseCardTest {

    @Test
    @DisplayName("Can tap for colorless mana")
    void canTapForColorlessMana() {
        harness.addToBattlefield(player1, new MouthOfRonom());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Mouth of Ronom");
    }

    @Test
    @DisplayName("Deals 4 damage to target creature and sacrifices itself")
    void dealsDamageAndSacrificesItself() {
        harness.addToBattlefield(player1, new MouthOfRonom());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());
        addAbilityMana();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mouth of Ronom");
        harness.assertInGraveyard(player2, "Boreal Centaur");
    }

    @Test
    @DisplayName("Deals exactly 4 damage to target creature")
    void dealsExactlyFourDamage() {
        harness.addToBattlefield(player1, new MouthOfRonom());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianSnowcrusher());
        addAbilityMana();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Phyrexian Snowcrusher");
        harness.assertInGraveyard(player1, "Mouth of Ronom");
    }

    @Test
    @DisplayName("Requires snow mana in addition to four generic mana")
    void requiresSnowMana() {
        harness.addToBattlefield(player1, new MouthOfRonom());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new MouthOfRonom());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealShelf());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice and mana payment happen before the damage resolves")
    void paysCostsBeforeResolvingDamage() {
        harness.addToBattlefield(player1, new MouthOfRonom());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianSnowcrusher());
        addAbilityMana();

        harness.activateAbility(player1, 0, 1, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Mouth of Ronom");
        harness.assertInGraveyard(player1, "Mouth of Ronom");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(target.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Can target a creature controlled by its controller")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new MouthOfRonom());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BorealCentaur());
        addAbilityMana();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boreal Centaur");
        harness.assertInGraveyard(player1, "Mouth of Ronom");
    }

    @Test
    @DisplayName("Cannot activate damage ability after tapping for mana")
    void cannotActivateDamageAbilityWhileTapped() {
        harness.addToBattlefield(player1, new MouthOfRonom());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());
        addAbilityMana();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player1, "Mouth of Ronom");
        harness.assertNotInGraveyard(player1, "Mouth of Ronom");
    }

    @Test
    @DisplayName("Snow mana alone does not replace the four generic mana")
    void requiresFourGenericManaInAdditionToSnow() {
        harness.addToBattlefield(player1, new MouthOfRonom());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertOnBattlefield(player1, "Mouth of Ronom");
    }

    @Test
    @DisplayName("Mana from another Mouth of Ronom pays the snow cost")
    void manaAbilityProducesSnowMana() {
        harness.addToBattlefield(player1, new MouthOfRonom());
        harness.addToBattlefield(player1, new MouthOfRonom());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 1, 0, null, null);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mouth of Ronom");
        harness.assertOnBattlefield(player1, "Mouth of Ronom");
        harness.assertInGraveyard(player2, "Boreal Centaur");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new MouthOfRonom());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mouth of Ronom");
        harness.assertLife(player2, 20);
    }
    @Test
    @DisplayName("Colored snow mana can pay the snow cost")
    void acceptsColoredSnowMana() {
        harness.addToBattlefield(player1, new MouthOfRonom());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealCentaur());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Boreal Centaur");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Losing the target does not refund the sacrificed land or mana")
    void losingTargetDoesNotRefundCosts() {
        harness.addToBattlefield(player1, new MouthOfRonom());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianSnowcrusher());
        addAbilityMana();
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);

        harness.passBothPriorities();

        harness.assertInHand(player2, "Phyrexian Snowcrusher");
        harness.assertInGraveyard(player1, "Mouth of Ronom");
        harness.assertNotOnBattlefield(player1, "Mouth of Ronom");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(target.getMarkedDamage()).isZero();
    }
    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
    }
}
