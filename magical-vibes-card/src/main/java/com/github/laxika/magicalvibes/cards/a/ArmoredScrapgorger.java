package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

@CardRegistration(set = "ONE", collectorNumber = "158")
public class ArmoredScrapgorger extends Card {

    public ArmoredScrapgorger() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsSourceCardPredicate(),
                        SequenceEffect.of(
                                new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD),
                                new PutCountersOnSelfEffect(CounterType.OIL))));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(3, CounterType.OIL),
                new StaticBoostEffect(3, 0, GrantScope.SELF)));
    }
}
