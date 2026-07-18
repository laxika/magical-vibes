package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SistersOfTheFlameTest extends BaseCardTest {

    // ===== Mana production =====

    @Test
    @DisplayName("Tapping Sisters of the Flame produces one red mana")
    void tappingProducesRedMana() {
        Permanent perm = new Permanent(new SistersOfTheFlame());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }
}
