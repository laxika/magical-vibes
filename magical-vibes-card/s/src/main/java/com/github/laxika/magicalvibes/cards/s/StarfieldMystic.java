package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "39")
@CardRegistration(set = "SOC", collectorNumber = "177")
@CardRegistration(set = "DSC", collectorNumber = "105")
@CardRegistration(set = "WOC", collectorNumber = "76")
public class StarfieldMystic extends Card {

    public StarfieldMystic() {
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardTypePredicate(CardType.ENCHANTMENT), 1, CostModificationScope.SELF));
        addTrigger(Triggers.dies(Triggers.ENCHANTMENT, ObjectRelation.YOU_CONTROL),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
