package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.k.KaisoMemoryOfLoyalty;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "3")
public class FaithfulSquire extends Card {

    public FaithfulSquire() {
        setBackFaceCard(new KaisoMemoryOfLoyalty());

        // "Whenever you cast a Spirit or Arcane spell, you may put a ki counter on this creature."
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.SPIRIT),
                                new CardSubtypePredicate(CardSubtype.ARCANE))),
                        List.of(new MayEffect(new PutCountersOnSelfEffect(CounterType.KI),
                                "Put a ki counter on Faithful Squire?"))));

        // "At the beginning of the end step, if there are two or more ki counters on this creature,
        // you may flip it." - every end step, not just yours; the counter count is an intervening-if
        // so it is checked both on trigger and on resolution.
        addEffect(EffectSlot.END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new SourceCounterThreshold(2, CounterType.KI),
                        new MayEffect(new TransformToBackFaceEffect(), "Flip Faithful Squire?")));
    }

    @Override
    public String getBackFaceClassName() {
        return "KaisoMemoryOfLoyalty";
    }
}
