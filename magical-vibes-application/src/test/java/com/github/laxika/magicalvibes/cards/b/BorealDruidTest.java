package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BorealDruid.class)
class BorealDruidTest extends BaseCardTest {

    @Test
    void tappingForManaAddsColorlessMana() {
        Permanent druid = addCreatureReady(player1, new BorealDruid());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(druid.isTapped()).isTrue();
    }

    @Test
    void alreadyTappedCannotTapForManaAgain() {
        Permanent druid = addCreatureReady(player1, new BorealDruid());

        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(druid.isTapped()).isTrue();
    }

    @Test
    void summoningSickCannotTapForMana() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        druid.setSummoningSick(true);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(druid.isTapped()).isFalse();
    }
}
