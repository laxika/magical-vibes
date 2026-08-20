package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedAllyCreatureEntersTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "STX", collectorNumber = "102")
public class FirstDayOfClass extends Card {

    public FirstDayOfClass() {
        CardEffect enteringCreatureEffect = SequenceEffect.of(
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
        addEffect(EffectSlot.SPELL, new RegisterDelayedAllyCreatureEntersTriggerEffect(enteringCreatureEffect));

        addEffect(EffectSlot.SPELL, new LearnEffect());
    }
}
