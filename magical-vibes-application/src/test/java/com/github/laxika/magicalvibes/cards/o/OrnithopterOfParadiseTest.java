package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(OrnithopterOfParadise.class)
class OrnithopterOfParadiseTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for one mana of any color")
    void tapsForAnyColor() {
        Permanent ornithopter = addCreatureReady(player1, new OrnithopterOfParadise());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(ornithopter.isTapped()).isTrue();
    }
}
