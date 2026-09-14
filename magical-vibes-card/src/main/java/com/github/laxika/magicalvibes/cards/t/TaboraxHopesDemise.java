package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;

@CardRegistration(set = "ZNR", collectorNumber = "129")
public class TaboraxHopesDemise extends Card {

    public TaboraxHopesDemise() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(5, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)));

        var cleric = new CardSubtypePredicate(CardSubtype.CLERIC);
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new TriggeringCardConditionalEffect(cleric, SequenceEffect.of(
                        new PutCountersOnSourceEffect(1, 1, 1),
                        new MayEffect(
                                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)),
                                "Draw a card?"))));
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new TriggeringCardConditionalEffect(new CardNotPredicate(cleric),
                        new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
