package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlpineMeadow.class})
class AlpineMeadowTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new AlpineMeadow()));
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        tapFor(ManaColor.RED);
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        tapFor(ManaColor.WHITE);
    }

    @Test
    void entersTappedWhenPutOntoBattlefield() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new AlpineMeadow());

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new AlpineMeadow()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void redManaCanPaySnowCosts() {
        tapFor(ManaColor.RED);

        assertThat(gd.playerManaPools.get(player1.getId()).getSnowMana(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void whiteManaCanPaySnowCosts() {
        tapFor(ManaColor.WHITE);

        assertThat(gd.playerManaPools.get(player1.getId()).getSnowMana(ManaColor.WHITE)).isEqualTo(1);
    }

    private void tapFor(ManaColor color) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new AlpineMeadow());
        land.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

}
