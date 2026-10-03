package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DruidOfTheAnima.class})
class DruidOfTheAnimaTest extends BaseCardTest {

    private Permanent addReadyDruid() {
        return addCreatureReady(player1, new DruidOfTheAnima());
    }

    @Test
    @DisplayName("Tapping for red mana adds {R}")
    void tapForRedMana() {
        Permanent druid = addReadyDruid();
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(druid.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for green mana adds {G}")
    void tapForGreenMana() {
        addReadyDruid();
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for white mana adds {W}")
    void tapForWhiteMana() {
        addReadyDruid();
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana abilities do not use the stack")
    void manaAbilitiesDoNotUseStack() {
        addReadyDruid();
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        addReadyDruid();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("Summoning sickness prevents every mana choice")
    void cannotActivateWhileSummoningSick(int abilityIndex) {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new DruidOfTheAnima());
        druid.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(druid.isTapped()).isFalse();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"0, RED", "1, GREEN", "2, WHITE"})
    @DisplayName("Each activation produces only one mana of the chosen color")
    void producesOnlyChosenColor(int abilityIndex, ManaColor chosenColor) {
        Permanent druid = addReadyDruid();

        harness.activateAbility(player1, 0, abilityIndex, null, null);

        assertThat(druid.isTapped()).isTrue();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color))
                    .isEqualTo(color == chosenColor ? 1 : 0);
            assertThat(gd.playerManaPools.get(player2.getId()).get(color)).isZero();
        }
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
