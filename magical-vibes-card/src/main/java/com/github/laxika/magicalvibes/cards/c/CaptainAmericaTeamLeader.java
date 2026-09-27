package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "5")
public class CaptainAmericaTeamLeader extends Card {

    public CaptainAmericaTeamLeader() {
        CardSubtypePredicate hero = new CardSubtypePredicate(CardSubtype.HERO);
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(hero, SequenceEffect.of(
                        new PutCounterOnReferencedPermanentEffect(
                                PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(hero,
                        new BoostEnteringCreatureEffect(0, 0,
                                Set.of(Keyword.VIGILANCE, Keyword.HASTE))));
    }
}
