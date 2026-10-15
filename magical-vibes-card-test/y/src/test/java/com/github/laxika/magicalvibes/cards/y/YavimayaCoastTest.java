package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.s.SamiteHealer;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YavimayaCoast.class, SamiteHealer.class})
class YavimayaCoastTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds {C} and deals no damage")
    void tapForColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new YavimayaCoast());
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Tapping for green mana adds {G} and deals 1 damage to controller")
    void tapForGreenMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new YavimayaCoast());
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("Tapping for blue mana adds {U} and deals 1 damage to controller")
    void tapForBlueMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new YavimayaCoast());
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("Pain-land damage is dealt only to its controller")
    void painLandDamageOnlyAffectsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new YavimayaCoast());

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Pain-land damage can be prevented while mana is still added")
    void painLandDamageCanBePrevented() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new SamiteHealer());
        harness.addToBattlefield(player1, new YavimayaCoast());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertLife(player1, 20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new YavimayaCoast());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Blue mana damage can be prevented without preventing mana production")
    void blueManaDamageCanBePrevented() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new SamiteHealer());
        harness.addToBattlefield(player1, new YavimayaCoast());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.assertLife(player1, 20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colorless mana preserves a prevention shield for a later colored activation")
    void colorlessManaDoesNotConsumePreventionShield() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new SamiteHealer());
        harness.addToBattlefield(player1, new YavimayaCoast());
        harness.addToBattlefield(player1, new YavimayaCoast());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertLife(player1, 20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(1);

        harness.activateAbility(player1, 2, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertLife(player1, 20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colored mana abilities are mana abilities and do not use the stack")
    void coloredManaAbilitiesDoNotUseStack() {
        harness.addToBattlefield(player1, new YavimayaCoast());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
