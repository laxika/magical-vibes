package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GoobbueGardener.class)
class GoobbueGardenerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Goobbue Gardener produces one green mana")
    void tappingProducesGreenMana() {
        Permanent gardener = harness.addToBattlefieldAndReturn(player1, new GoobbueGardener());
        gardener.setSummoningSick(false);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gardener.isTapped()).isTrue();
    }
}
