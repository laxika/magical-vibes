package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NoxiousNewtTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Noxious Newt produces one green mana")
    void tappingProducesGreenMana() {
        Permanent newt = new Permanent(new NoxiousNewt());
        newt.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(newt);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(newt.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick Noxious Newt cannot tap for mana")
    void summoningSickCannotTap() {
        Permanent newt = new Permanent(new NoxiousNewt());
        newt.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(newt);

        assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(newt.isTapped()).isFalse();
    }
}
