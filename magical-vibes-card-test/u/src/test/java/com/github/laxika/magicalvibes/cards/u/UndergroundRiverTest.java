package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.Bandage;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UndergroundRiver.class, Bandage.class})
class UndergroundRiverTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds {C} and deals no damage")
    void tapForColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new UndergroundRiver());
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Tapping for blue mana adds {U} and deals 1 damage to controller")
    void tapForBlueMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new UndergroundRiver());
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Tapping for black mana adds {B} and deals 1 damage to controller")
    void tapForBlackMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new UndergroundRiver());
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Pain-land damage is dealt only to its controller")
    void painLandDamageOnlyAffectsController() {
        harness.addToBattlefield(player1, new UndergroundRiver());
        GameData gd = harness.getGameData();
        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int player2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1LifeBefore - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2LifeBefore);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new UndergroundRiver());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Colored mana abilities are mana abilities and do not use the stack")
    void coloredManaAbilitiesDoNotUseStack() {
        harness.addToBattlefield(player1, new UndergroundRiver());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Preventing pain-land damage does not prevent colored mana production")
    void preventedDamageStillProducesColoredMana(int abilityIndex) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new UndergroundRiver());
        harness.setLibrary(player1, List.of(new UndergroundRiver()));
        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, abilityIndex, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore);
        ManaColor color = abilityIndex == 1 ? ManaColor.BLUE : ManaColor.BLACK;
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Colored mana can be produced at one life because the damage is not a life payment")
    void coloredManaCanBeProducedAtOneLife() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new UndergroundRiver());
        harness.setLife(player1, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.assertLife(player1, 0);
    }
}
