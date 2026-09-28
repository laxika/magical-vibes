package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasNoNonKeywordAbilitiesPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MB2", collectorNumber = "317")
@CardRegistration(set = "MB2", collectorNumber = "553")
public class TLDR extends Card {

    public TLDR() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new ConditionalEffect(
                        new NotCondition(new TargetPermanentMatches(
                                new PermanentHasNoNonKeywordAbilitiesPredicate())),
                        new ExileTargetPermanentEffect()));
    }
}
