package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetWhileHasCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LRW", collectorNumber = "52")
@CardRegistration(set = "DDT", collectorNumber = "2")
public class AquitectsWill extends Card {

    public AquitectsWill() {
        target(TargetFilters.land()).addEffect(EffectSlot.SPELL,
                new PutCounterOnTargetPermanentEffect(CounterType.FLOOD, 1));
        addEffect(EffectSlot.SPELL,
                new GrantSubtypeToTargetWhileHasCounterEffect(CardSubtype.ISLAND, CounterType.FLOOD));
        // If you control a Merfolk, draw a card.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControlsPermanentCount(1, new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)),
                new DrawCardEffect()));
    }
}
