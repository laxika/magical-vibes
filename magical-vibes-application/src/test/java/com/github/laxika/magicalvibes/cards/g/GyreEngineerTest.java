package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GyreEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Gyre Engineer produces one green and one blue mana")
    void tappingProducesGreenAndBlueMana() {
        Permanent perm = new Permanent(new GyreEngineer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick Gyre Engineer cannot tap for mana")
    void summoningSickCannotTap() {
        Permanent perm = new Permanent(new GyreEngineer());
        perm.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }
}
