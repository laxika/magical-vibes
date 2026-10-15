package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "113")
public class VadmirNewBlood extends Card {

    public VadmirNewBlood() {
        addEffect(EffectSlot.ON_CONTROLLER_COMMITS_CRIME,
                new OncePerTurnTriggerEffect(new PutCountersOnSourceEffect(1, 1, 1)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(4, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Set.of(Keyword.MENACE, Keyword.LIFELINK), GrantScope.SELF)));
    }
}
