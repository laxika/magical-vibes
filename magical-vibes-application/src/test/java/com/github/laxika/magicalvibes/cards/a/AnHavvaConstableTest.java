package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Diminish;
import com.github.laxika.magicalvibes.cards.f.FolkOfAnHavva;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnHavvaConstable.class, AnabaAncestor.class, FolkOfAnHavva.class})
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
        addCreatureReady(player1, new FolkOfAnHavva());
        addCreatureReady(player1, new FolkOfAnHavva());

        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(4);
    }

    @Test
    @DisplayName("Green creatures on any battlefield count")
    void opponentGreenCreaturesCount() {
        Permanent constable = addCreatureReady(player1, new AnHavvaConstable());
        addCreatureReady(player2, new FolkOfAnHavva());

        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-green creatures do not count")
    void nonGreenCreaturesDontCount() {
        Permanent constable = addCreatureReady(player1, new AnHavvaConstable());
        addCreatureReady(player1, new AnabaAncestor());

        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(2);
    }

    @Test
    @CardUsed(Diminish.class)
    @DisplayName("A base P/T setter overrides its characteristic-defining toughness")
    void basePowerToughnessSetterOverridesCharacteristicDefiningToughness() {
        Permanent constable = addCreatureReady(player1, new AnHavvaConstable());
        addCreatureReady(player1, new FolkOfAnHavva());

        harness.setHand(player1, List.of(new Diminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, constable.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, constable)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, constable)).isEqualTo(1);
    }
}
