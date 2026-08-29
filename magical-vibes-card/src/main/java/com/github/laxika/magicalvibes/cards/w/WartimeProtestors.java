package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "160")
public class WartimeProtestors extends Card {

    public WartimeProtestors() {
        // Whenever another Ally you control enters, put a +1/+1 counter on that creature and it
        // gains haste until end of turn.
        CardSubtypePredicate ally = new CardSubtypePredicate(CardSubtype.ALLY);
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(ally,
                        new PutCountersOnEnteringCreatureEffect(1, false)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(ally,
                        new BoostEnteringCreatureEffect(0, 0, Set.of(Keyword.HASTE))));
    }
}
