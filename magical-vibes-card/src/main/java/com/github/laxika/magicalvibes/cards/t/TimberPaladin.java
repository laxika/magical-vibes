package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.EnchantedByAtLeastAuras;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOC", collectorNumber = "20")
@CardRegistration(set = "WOC", collectorNumber = "56")
public class TimberPaladin extends Card {

    public TimberPaladin() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                exactlyAuras(1), new SetBasePowerToughnessEffect(3, 3, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                exactlyAuras(2), new SetBasePowerToughnessEffect(5, 5, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new EnchantedByAtLeastAuras(3),
                new SetBasePowerToughnessEffect(10, 10, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new EnchantedByAtLeastAuras(3),
                new GrantKeywordEffect(Set.of(Keyword.VIGILANCE, Keyword.TRAMPLE), GrantScope.SELF)));
    }

    private static AllOf exactlyAuras(int count) {
        return new AllOf(List.of(
                new EnchantedByAtLeastAuras(count),
                new NotCondition(new EnchantedByAtLeastAuras(count + 1))));
    }
}
