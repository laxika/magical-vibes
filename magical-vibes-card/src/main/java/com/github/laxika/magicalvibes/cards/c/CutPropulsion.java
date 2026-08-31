package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EOE", collectorNumber = "130")
public class CutPropulsion extends Card {

    public CutPropulsion() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new ConditionalReplacementEffect(
                        new TargetPermanentMatches(new PermanentHasKeywordPredicate(Keyword.FLYING)),
                        new TargetCreatureDealsPowerDamageToSelfEffect(),
                        new TargetCreatureDealsPowerDamageToSelfEffect(2)));
    }
}
