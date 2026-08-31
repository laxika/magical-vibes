package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DryadArbor.class)
class DryadArborTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Dryad Arbor produces one green mana")
    void tappingProducesGreenMana() {
        Permanent perm = new Permanent(new DryadArbor());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(perm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick Dryad Arbor cannot tap for mana")
    void summoningSickCannotTap() {
        Permanent perm = new Permanent(new DryadArbor());
        perm.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(perm.isTapped()).isFalse();
    }
}
