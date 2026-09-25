package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "2454")
public class MutationalAdvantage extends Card {

    public MutationalAdvantage() {
        PermanentHasCountersPredicate hasCounters = new PermanentHasCountersPredicate(CounterType.ANY);
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(
                Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE), GrantScope.OWN_PERMANENTS, hasCounters));
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allToControlledMatchingPermanents(hasCounters));
        addEffect(EffectSlot.SPELL, new ProliferateEffect());
    }
}
