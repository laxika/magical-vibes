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

@CardRegistration(set = "PIP", collectorNumber = "111")
@CardRegistration(set = "PIP", collectorNumber = "422")
@CardRegistration(set = "PIP", collectorNumber = "639")
@CardRegistration(set = "PIP", collectorNumber = "950")
public class MutationalAdvantage extends Card {

    public MutationalAdvantage() {
        var permanentsWithCounters = new PermanentHasCountersPredicate(CounterType.ANY);
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(
                Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE), GrantScope.OWN_PERMANENTS,
                permanentsWithCounters));
        addEffect(EffectSlot.SPELL,
                PreventDamageEffect.allToControlledMatchingPermanents(permanentsWithCounters));
        addEffect(EffectSlot.SPELL, new ProliferateEffect());
    }
}
