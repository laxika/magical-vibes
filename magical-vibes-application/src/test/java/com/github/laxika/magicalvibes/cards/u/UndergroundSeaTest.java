package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(UndergroundSea.class)
class UndergroundSeaTest extends BaseCardTest {

    @Test
    @DisplayName("Underground Sea produces blue mana")
    void producesBlueMana() {
        Permanent undergroundSea = addUndergroundSeaReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(undergroundSea.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Underground Sea produces black mana")
    void producesBlackMana() {
        Permanent undergroundSea = addUndergroundSeaReady();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(undergroundSea.isTapped()).isTrue();
    }

    private Permanent addUndergroundSeaReady() {
        Permanent undergroundSea = harness.addToBattlefieldAndReturn(player1, new UndergroundSea());
        undergroundSea.setSummoningSick(false);
        return undergroundSea;
    }
}
