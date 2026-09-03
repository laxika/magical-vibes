package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SunastianFalconer.class)
class SunastianFalconerTest extends BaseCardTest {

    @Test
    void tappingAddsTwoColorlessMana() {
        Permanent falconer = harness.addToBattlefieldAndReturn(player1, new SunastianFalconer());
        falconer.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(falconer.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
