package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerGainsControlOfOwnedPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "CSP", collectorNumber = "106")
public class BroodingSaurian extends Card {

    public BroodingSaurian() {
        addEffect(EffectSlot.END_STEP_TRIGGERED,
                new EachPlayerGainsControlOfOwnedPermanentsMatchingEffect(
                        new PermanentNotPredicate(new PermanentIsTokenPredicate())));
    }
}
