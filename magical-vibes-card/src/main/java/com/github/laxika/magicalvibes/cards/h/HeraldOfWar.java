package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "24")
public class HeraldOfWar extends Card {

    public HeraldOfWar() {
        // Flying — loaded from Scryfall
        // Whenever this creature attacks, put a +1/+1 counter on it
        addEffect(EffectSlot.ON_ATTACK, new PutCountersOnSourceEffect(1, 1, 1));
        // Angel spells and Human spells you cast cost {1} less to cast for each +1/+1 counter on this creature
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.ANGEL),
                        new CardSubtypePredicate(CardSubtype.HUMAN)
                )),
                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                CostModificationScope.SELF));
    }
}
