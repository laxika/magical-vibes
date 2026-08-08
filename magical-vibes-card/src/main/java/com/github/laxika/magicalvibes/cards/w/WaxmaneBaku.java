package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "29")
public class WaxmaneBaku extends Card {

    public WaxmaneBaku() {
        // Whenever you cast a Spirit or Arcane spell, you may put a ki counter on this creature.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.SPIRIT),
                                new CardSubtypePredicate(CardSubtype.ARCANE))),
                        List.of(new MayEffect(new PutCountersOnSelfEffect(CounterType.KI),
                                "Put a ki counter on this creature?"))));

        // {1}, Remove X ki counters from this creature: Tap X target creatures. X is announced at
        // activation (capped by the ki counters present) and bounds the target count via
        // withXScaledTargets; the tap handler fans over the whole chosen target group.
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new RemoveXCountersFromSourceCost(CounterType.KI),
                        new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{1}, Remove X ki counters from this creature: Tap X target creatures.",
                TargetFilters.creature(),
                null, null, null, List.of(), 0, 100)
                .withXScaledTargets());
    }
}
