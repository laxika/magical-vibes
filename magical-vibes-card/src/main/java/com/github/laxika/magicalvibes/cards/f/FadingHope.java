package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MID", collectorNumber = "51")
public class FadingHope extends Card {

    public FadingHope() {
        var lowManaValue = new TargetPermanentMatches(new PermanentMaxManaValuePredicate(3));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(lowManaValue,
                        SequenceEffect.of(ReturnToHandEffect.target(), new ScryEffect(1))))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new NotCondition(lowManaValue), ReturnToHandEffect.target()));
    }
}
