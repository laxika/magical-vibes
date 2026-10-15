package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "EOE", collectorNumber = "71")
public class NanoformSentinel extends Card {

    public NanoformSentinel() {
        PermanentPredicate another = new PermanentNotPredicate(new PermanentIsSourceCardPredicate());

        target(new PermanentPredicateTargetFilter(another, "Target must be another permanent"))
                .addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED,
                        new TriggeringPermanentConditionalEffect(
                                new PermanentIsSourceCardPredicate(),
                                new OncePerTurnTriggerEffect(
                                        new UntapPermanentsEffect(TapUntapScope.TARGET, another))));
    }
}
