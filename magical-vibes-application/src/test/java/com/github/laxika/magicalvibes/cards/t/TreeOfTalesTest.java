package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TreeOfTales.class)
class TreeOfTalesTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds {G}")
    void tapForGreenMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TreeOfTales());

        harness.activateAbility(player1, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tree of Tales can produce mana immediately after being played")
    void canProduceManaOnTurnPlayed() {
        harness.setHand(player1, List.of(new TreeOfTales()));

        harness.playLand(player1, 0);

        harness.assertOnBattlefield(player1, "Tree of Tales");
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Tree of Tales cannot produce mana again")
    void cannotActivateWhileTapped() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TreeOfTales());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
