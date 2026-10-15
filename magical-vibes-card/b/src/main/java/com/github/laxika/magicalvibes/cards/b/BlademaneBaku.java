package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "95")
public class BlademaneBaku extends Card {

    public BlademaneBaku() {
        // Whenever you cast a Spirit or Arcane spell, you may put a ki counter on this creature.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.SPIRIT),
                                new CardSubtypePredicate(CardSubtype.ARCANE))),
                        List.of(new MayEffect(new PutCountersOnSelfEffect(CounterType.KI),
                                "Put a ki counter on this creature?"))));

        // {1}, Remove X ki counters from this creature: For each counter removed, this creature gets
        // +2/+0 until end of turn. X is announced at activation (capped by the ki counters present)
        // and drives the boost via Scaled(XValue, 2).
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new RemoveXCountersFromSourceCost(CounterType.KI),
                        new BoostSelfEffect(new Scaled(new XValue(), 2), new Fixed(0))),
                "{1}, Remove X ki counters from this creature: For each counter removed, this "
                        + "creature gets +2/+0 until end of turn."));
    }
}
