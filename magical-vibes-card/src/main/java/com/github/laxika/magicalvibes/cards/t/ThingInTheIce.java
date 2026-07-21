package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AwokenHorror;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Thing in the Ice — front face of Thing in the Ice // Awoken Horror.
 * Creature — Horror {1}{U}
 * Defender
 * This creature enters with four ice counters on it.
 * Whenever you cast an instant or sorcery spell, remove an ice counter from this creature.
 * Then if it has no ice counters on it, transform it.
 */
@CardRegistration(set = "INR", collectorNumber = "91")
public class ThingInTheIce extends Card {

    public ThingInTheIce() {
        AwokenHorror backFace = new AwokenHorror();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // This creature enters with four ice counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.ICE, new Fixed(4)));

        // Whenever you cast an instant or sorcery spell, remove an ice counter from this creature.
        // Then if it has no ice counters on it, transform it.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )),
                List.of(SequenceEffect.of(
                        new RemoveCounterFromSourceEffect(CounterType.ICE, 1),
                        new ConditionalEffect(
                                new NotCondition(new SourceCounterThreshold(1, CounterType.ICE)),
                                new TransformSelfEffect())))));
    }

    @Override
    public String getBackFaceClassName() {
        return "AwokenHorror";
    }
}
