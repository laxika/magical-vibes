package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.UnleashEffect;

/**
 * Chaos Imps — {4}{R}{R} Creature — Imp 6/5 with flying and unleash.
 * <p>
 * Flying is a Scryfall-loaded keyword. Unleash is two static abilities: the optional
 * as-enters +1/+1 counter ({@link UnleashEffect}) and "can't block as long as it has a
 * +1/+1 counter on it". The conditional trample keys off the same counter.
 */
@CardRegistration(set = "RTR", collectorNumber = "90")
public class ChaosImps extends Card {

    public ChaosImps() {
        addEffect(EffectSlot.STATIC, new UnleashEffect());
        addEffect(EffectSlot.STATIC, new CantBlockUnlessEffect(
                new NotCondition(new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE)),
                "it has no +1/+1 counters on it"));

        // This creature has trample as long as it has a +1/+1 counter on it.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)));
    }
}
