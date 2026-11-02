package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.condition.SourcePowerParity;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSC", collectorNumber = "2")
public class KianneCorruptedMemory extends Card {

    public KianneCorruptedMemory() {
        CardNotPredicate noncreatureSpell = new CardNotPredicate(new CardTypePredicate(CardType.CREATURE));
        addStaticEffect(new ConditionalEffect(
                new SourcePowerParity(ManaValueParity.EVEN),
                new GrantFlashToCardTypeEffect(noncreatureSpell)));
        addStaticEffect(new ConditionalEffect(
                new SourcePowerParity(ManaValueParity.ODD),
                new GrantFlashToCardTypeEffect(new CardTypePredicate(CardType.CREATURE))));
        addTrigger(Triggers.youDrawCard(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
