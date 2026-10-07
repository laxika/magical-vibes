package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentCantBeTargetedByOpponentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "98")
public class CanopyCover extends Card {

    public CanopyCover() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentHasKeywordPredicate(Keyword.FLYING),
                                new PermanentHasKeywordPredicate(Keyword.REACH)
                        )),
                        "creatures with flying or reach"))
                // "your opponents" are the Aura controller's opponents, not the creature controller's
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentCantBeTargetedByOpponentsEffect());
    }
}
