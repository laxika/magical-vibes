package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WirewoodElf.class)
class WirewoodElfTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Wirewood Elf produces one green mana")
    void tappingProducesGreenMana() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new WirewoodElf());
        elf.setSummoningSick(false);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(elf.isTapped()).isTrue();
    }
}
