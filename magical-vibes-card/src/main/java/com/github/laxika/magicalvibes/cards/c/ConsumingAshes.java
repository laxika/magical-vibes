package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OTJ", collectorNumber = "83")
public class ConsumingAshes extends Card {

    public ConsumingAshes() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new ExileTargetPermanentThenEffect(
                new SurveilEffect(2), ThenEffectRecipient.CONTROLLER,
                new PermanentMaxManaValuePredicate(3)));
    }
}
