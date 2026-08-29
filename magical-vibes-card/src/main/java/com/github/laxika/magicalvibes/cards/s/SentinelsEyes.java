package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "36")
public class SentinelsEyes extends Card {

    public SentinelsEyes() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        1, 1, Set.of(Keyword.VIGILANCE), GrantScope.ENCHANTED_CREATURE));
        addCastingOption(new GraveyardCast(null, "{W}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 2))));
    }
}
