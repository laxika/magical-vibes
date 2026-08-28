package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "138")
public class WitchMawNephilim extends Card {

    public WitchMawNephilim() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(null, List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2))),
                "Put two +1/+1 counters on this creature?"));
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new SourcePowerAtLeast(10),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)));
    }
}
