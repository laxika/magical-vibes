package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LCI", collectorNumber = "46")
public class BrackishBlunder extends Card {

    public BrackishBlunder() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, SequenceEffect.of(
                new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentIsTappedPredicate()),
                        CreateTokenEffect.ofMapToken(1)),
                ReturnToHandEffect.target()));
    }
}
