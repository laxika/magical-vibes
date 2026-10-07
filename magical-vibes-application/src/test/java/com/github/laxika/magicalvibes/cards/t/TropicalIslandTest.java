package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TropicalIsland.class})
class TropicalIslandTest extends BaseCardTest {

    @Test
    @DisplayName("Tropical Island produces green mana")
    void producesGreenMana() {
        Permanent tropicalIsland = addTropicalIslandReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(tropicalIsland.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tropical Island produces blue mana")
    void producesBlueMana() {
        Permanent tropicalIsland = addTropicalIslandReady();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(tropicalIsland.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tropical Island cannot activate its other mana ability while tapped")
    void cannotActivateOtherManaAbilityWhileTapped() {
        Permanent tropicalIsland = addTropicalIslandReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(tropicalIsland.isTapped()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("A newly entered Tropical Island produces either color immediately without using the stack")
    void newlyEnteredLandProducesManaImmediately(int abilityIndex) {
        Permanent tropicalIsland = harness.enterBattlefieldAndReturn(player1, new TropicalIsland());
        ManaColor chosenColor = abilityIndex == 0 ? ManaColor.GREEN : ManaColor.BLUE;
        ManaColor otherColor = abilityIndex == 0 ? ManaColor.BLUE : ManaColor.GREEN;

        harness.activateAbility(player1, 0, abilityIndex, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(chosenColor)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(otherColor)).isZero();
        assertThat(tropicalIsland.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addTropicalIslandReady() {
        Permanent tropicalIsland = harness.addToBattlefieldAndReturn(player1, new TropicalIsland());
        tropicalIsland.setSummoningSick(false);
        return tropicalIsland;
    }
}
