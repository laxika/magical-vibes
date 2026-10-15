package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "83")
public class SkullmaneBaku extends Card {

    public SkullmaneBaku() {
        // Whenever you cast a Spirit or Arcane spell, you may put a ki counter on this creature.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.SPIRIT),
                                new CardSubtypePredicate(CardSubtype.ARCANE))),
                        List.of(new MayEffect(new PutCountersOnSelfEffect(CounterType.KI),
                                "Put a ki counter on this creature?"))));

        // {1}, {T}, Remove X ki counters from this creature: Target creature gets -X/-X until end of
        // turn. X is announced at activation (capped by the ki counters present) and drives the
        // boost amount via Scaled(XValue, -1).
        addActivatedAbility(new ActivatedAbility(true, "{1}",
                List.of(new RemoveXCountersFromSourceCost(CounterType.KI),
                        new BoostTargetCreatureEffect(
                                new Scaled(new XValue(), -1),
                                new Scaled(new XValue(), -1))),
                "{1}, {T}, Remove X ki counters from this creature: Target creature gets -X/-X until "
                        + "end of turn.",
                TargetFilters.creature()));
    }
}
