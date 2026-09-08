package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SorceressQueen;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnHavvaConstable.class, GrizzlyBears.class, SorceressQueen.class})
class AnHavvaConstableTest extends BaseCardTest {

    @Test
    @DisplayName("Alone it counts itself: 2/2")
    void aloneCountsItself() {
        Permanent constable = addCreatureReady(player1, new AnHavvaConstable());

        assertThat(gqs.getEffectivePower(gd, constable)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each green creature adds one toughness")
    void greenCreaturesAddToughness() {
        Permanent constable = addCreatureReady(player1, new AnHavvaConstable());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, constable)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(4);
    }

    @Test
    @DisplayName("Green creatures on any battlefield count")
    void opponentGreenCreaturesCount() {
        Permanent constable = addCreatureReady(player1, new AnHavvaConstable());
        addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-green creatures do not count")
    void nonGreenCreaturesDontCount() {
        Permanent constable = addCreatureReady(player1, new AnHavvaConstable());
        addCreatureReady(player1, new SorceressQueen());

        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(2);
    }

    @Test
    @DisplayName("A base P/T setter overrides its characteristic-defining toughness")
    void basePowerToughnessSetterOverridesCharacteristicDefiningToughness() {
        addCreatureReady(player1, new SorceressQueen());
        Permanent constable = addCreatureReady(player1, new AnHavvaConstable());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, constable.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, constable)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(2);
    }
}
