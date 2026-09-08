package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

@CardRegistration(set = "JUD", collectorNumber = "44")
public class LaquatussDisdain extends Card {

    public LaquatussDisdain() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryCastFromZonePredicate(Zone.GRAVEYARD),
                "Target must be a spell cast from a graveyard."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect())
                .addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
