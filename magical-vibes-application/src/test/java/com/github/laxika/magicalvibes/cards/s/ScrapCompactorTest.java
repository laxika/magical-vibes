package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrapCompactor.class, AirElemental.class, DuskLegionDreadnought.class, Forest.class})
class ScrapCompactorTest extends BaseCardTest {

    @Test
    @DisplayName("The damage ability sacrifices Scrap Compactor and deals 3 damage to a creature")
    void dealsDamageToTargetCreature() {
        harness.addToBattlefield(player1, new ScrapCompactor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Scrap Compactor");
    }

    @Test
    @DisplayName("The destruction ability sacrifices Scrap Compactor and destroys a Vehicle")
    void destroysTargetVehicle() {
        harness.addToBattlefield(player1, new ScrapCompactor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scrap Compactor");
        harness.assertInGraveyard(player2, "Dusk Legion Dreadnought");
    }

    @Test
    @DisplayName("The damage ability cannot target a noncreature Vehicle")
    void damageAbilityCannotTargetVehicle() {
        harness.addToBattlefield(player1, new ScrapCompactor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The destruction ability cannot target a land")
    void destructionAbilityCannotTargetLand() {
        harness.addToBattlefield(player1, new ScrapCompactor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The destruction ability can destroy its controller's creature")
    void destroysOwnCreature() {
        harness.addToBattlefield(player1, new ScrapCompactor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scrap Compactor");
        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertNotOnBattlefield(player1, "Air Elemental");
    }

    @ParameterizedTest
    @CsvSource({"0, 3", "1, 6"})
    @DisplayName("Sacrifice is paid before resolution and a newly entered noncreature artifact can activate")
    void paysSacrificeBeforeResolution(int abilityIndex, int mana) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScrapCompactor());
        source.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, mana);

        harness.activateAbility(player1, 0, abilityIndex, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Scrap Compactor");
        harness.assertInGraveyard(player1, "Scrap Compactor");
        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(target.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        if (abilityIndex == 0) {
            assertThat(target.getMarkedDamage()).isEqualTo(3);
        } else {
            harness.assertInGraveyard(player2, "Air Elemental");
        }
    }

    @ParameterizedTest
    @CsvSource({"0, 3", "1, 6"})
    @DisplayName("A tapped Scrap Compactor cannot activate either ability")
    void cannotActivateWhenTapped(int abilityIndex, int mana) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScrapCompactor());
        source.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, mana);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Scrap Compactor");
        harness.assertNotInGraveyard(player1, "Scrap Compactor");
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"0, 2", "1, 5"})
    @DisplayName("Neither ability can be activated without its full mana cost")
    void cannotActivateWithInsufficientMana(int abilityIndex, int mana) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScrapCompactor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, mana);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Scrap Compactor");
        harness.assertNotInGraveyard(player1, "Scrap Compactor");
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"0, 3", "1, 6"})
    @DisplayName("Neither ability can target a player")
    void cannotTargetPlayer(int abilityIndex, int mana) {
        harness.addToBattlefield(player1, new ScrapCompactor());
        harness.addMana(player1, ManaColor.COLORLESS, mana);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Scrap Compactor");
        harness.assertNotInGraveyard(player1, "Scrap Compactor");
    }

    @ParameterizedTest
    @CsvSource({"0, 3", "1, 6"})
    @DisplayName("Removing the target in response does not refund the sacrificed artifact")
    void targetDestroyedInResponse(int abilityIndex, int mana) {
        harness.addToBattlefield(player1, new ScrapCompactor());
        harness.addToBattlefield(player2, new ScrapCompactor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, mana);
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, abilityIndex, null, target.getId());
        harness.activateAbility(player2, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scrap Compactor");
        harness.assertInGraveyard(player2, "Scrap Compactor");
        assertThat(gd.stack).isEmpty();
    }
}
