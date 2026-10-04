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
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LCI", collectorNumber = "46")
public class BrackishBlunder extends Card {

    public BrackishBlunder() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentNotPredicate(new PermanentIsTappedPredicate())),
                        ReturnToHandEffect.target(), false))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentIsTappedPredicate()),
                        SequenceEffect.of(ReturnToHandEffect.target(), CreateTokenEffect.ofMapToken(1)), false));
    }
}
