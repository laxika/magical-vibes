package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UlnaAlleyShopkeepTest extends BaseCardTest {

    

    @Test
    @DisplayName("No bonus when you have not gained life this turn")
    void noBonusWithoutLifeGain() {
        harness.addToBattlefield(player1, new UlnaAlleyShopkeep());

        Permanent shopkeep = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, shopkeep)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shopkeep)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +2/+0 while you have gained life this turn")
    void bonusWhileLifeGained() {
        harness.addToBattlefield(player1, new UlnaAlleyShopkeep());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        Permanent shopkeep = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, shopkeep)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, shopkeep)).isEqualTo(3);
    }
}
