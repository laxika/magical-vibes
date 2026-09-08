package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ColorMostCommonAmongAllPermanents;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PLS", collectorNumber = "6")
public class HeroicDefiance extends Card {

    public HeroicDefiance() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new ConditionalEffect(
                        new NotCondition(sharesMostCommonColor()),
                        new StaticBoostEffect(3, 3, GrantScope.ENCHANTED_CREATURE)));
    }

    private static Condition sharesMostCommonColor() {
        return new AnyOf(List.of(
                sharesMostCommonColor(CardColor.WHITE, "white"),
                sharesMostCommonColor(CardColor.BLUE, "blue"),
                sharesMostCommonColor(CardColor.BLACK, "black"),
                sharesMostCommonColor(CardColor.RED, "red"),
                sharesMostCommonColor(CardColor.GREEN, "green")));
    }

    private static Condition sharesMostCommonColor(CardColor color, String colorName) {
        return new AllConditions(List.of(
                new EnchantedPermanentMatches(
                        new PermanentColorInPredicate(Set.of(color)),
                        "enchanted creature is " + colorName),
                new ColorMostCommonAmongAllPermanents(color)));
    }
}
