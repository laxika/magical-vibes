package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed(TigereyeCameo.class)
class TigereyeCameoTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds green mana")
    void tapForGreenMana() {
        Permanent cameo = harness.addToBattlefieldAndReturn(player1, new TigereyeCameo());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(cameo.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Tapping adds white mana")
    void tapForWhiteMana() {
        Permanent cameo = harness.addToBattlefieldAndReturn(player1, new TigereyeCameo());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(cameo.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana abilities do not use the stack")
    void manaAbilitiesDoNotUseStack() {
        harness.addToBattlefield(player1, new TigereyeCameo());
        GameData gameData = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gameData.interaction.activeInteraction()).isNull();
        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gameData.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("A tapped Cameo cannot produce either color")
    void tappedCameoCannotProduceMana(int abilityIndex) {
        Permanent cameo = harness.addToBattlefieldAndReturn(player1, new TigereyeCameo());
        cameo.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(cameo.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Producing one color pays the tap cost for both color options")
    void cannotProduceBothColorsWithoutUntapping(int abilityIndex) {
        Permanent cameo = harness.addToBattlefieldAndReturn(player1, new TigereyeCameo());

        harness.activateAbility(player1, 0, abilityIndex, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1 - abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(cameo.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN))
                .isEqualTo(abilityIndex == 0 ? 1 : 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE))
                .isEqualTo(abilityIndex == 1 ? 1 : 0);
        assertThat(gd.stack).isEmpty();
    }
}
