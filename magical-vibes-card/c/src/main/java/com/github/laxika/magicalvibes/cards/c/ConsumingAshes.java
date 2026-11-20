package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OTJ", collectorNumber = "83")
public class ConsumingAshes extends Card {

    public ConsumingAshes() {
        target(TargetFilters.creature()).addSpellEffect(new ExileTargetPermanentEffect())
                .addSpellEffect(new ConditionalEffect(new TargetPermanentMatches(new PermanentMaxManaValuePredicate(3)), new SurveilEffect(2)));
    }
}
