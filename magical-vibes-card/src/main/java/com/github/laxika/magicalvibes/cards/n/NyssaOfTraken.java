package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WHO", collectorNumber = "51")
@CardRegistration(set = "WHO", collectorNumber = "366")
public class NyssaOfTraken extends Card {

    public NyssaOfTraken() {
        addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());

        targetUpTo(new EventValue(), TargetFilters.creature(), 100)
                .addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                        new SacrificeAnyNumberOfPermanentsEffect(new PermanentIsArtifactPredicate()),
                        ConditionalEffect.unless(
                                new EventValueAtLeast(1),
                                new QueueReflexiveAbilityEffect(
                                        SequenceEffect.of(
                                                new TapPermanentsEffect(TapUntapScope.TARGET),
                                                new DrawCardEffect(new EventValue())),
                                        false,
                                        true))));
    }
}
