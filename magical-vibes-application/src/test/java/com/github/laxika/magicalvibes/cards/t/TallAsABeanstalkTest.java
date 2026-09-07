package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TallAsABeanstalk.class, GrizzlyBears.class, FountainOfYouth.class})
class TallAsABeanstalkTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+3, reach, and Giant in addition to its other types")
    void grantsBoostReachAndGiantSubtype() {
        Permanent bears = addBears();
        attachAura(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isTrue();
        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, bears);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.GIANT);
        assertThat(bonus.subtypeOverriding()).isFalse();
    }

    @Test
    @DisplayName("Tall as a Beanstalk's effects stop when it leaves the battlefield")
    void effectsStopWhenAuraLeaves() {
        Permanent bears = addBears();
        Permanent aura = attachAura(bears);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, bears).grantedSubtypes()).doesNotContain(CardSubtype.GIANT);
    }

    @Test
    @DisplayName("Tall as a Beanstalk cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new TallAsABeanstalk()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addBears() {
        return harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
    }

    private Permanent attachAura(Permanent enchanted) {
        Permanent aura = new Permanent(new TallAsABeanstalk());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
