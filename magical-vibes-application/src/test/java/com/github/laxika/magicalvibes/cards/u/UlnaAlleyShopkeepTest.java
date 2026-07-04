package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UlnaAlleyShopkeepTest extends BaseCardTest {

    @Test
    @DisplayName("Has a conditional static +2/+0 self boost gated on gaining life")
    void hasCorrectEffect() {
        UlnaAlleyShopkeep card = new UlnaAlleyShopkeep();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(ConditionalEffect.class);

        ConditionalEffect conditional = (ConditionalEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(conditional.condition()).isInstanceOf(GainedLifeThisTurn.class);
        assertThat(conditional.wrapped()).isInstanceOf(StaticBoostEffect.class);
        StaticBoostEffect boost = (StaticBoostEffect) conditional.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
        assertThat(boost.scope()).isEqualTo(GrantScope.SELF);
    }

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
