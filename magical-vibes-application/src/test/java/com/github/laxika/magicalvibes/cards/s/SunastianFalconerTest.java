package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunastianFalconer.class})
class SunastianFalconerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Sunastian Falconer adds two colorless mana")
    void tappingAddsTwoColorlessMana() {
        Permanent falconer = addCreatureReady(player1, new SunastianFalconer());

        harness.activateAbility(player1, 0, null, null);

        assertThat(falconer.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sunastian Falconer's mana ability cannot be activated while it has summoning sickness")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new SunastianFalconer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Sunastian Falconer's mana ability cannot be activated while tapped")
    void cannotActivateWhileTapped() {
        Permanent falconer = addCreatureReady(player1, new SunastianFalconer());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(falconer.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }
}
