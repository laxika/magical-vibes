package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.SpellTypeTargetFilter;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "100")
public class RemoveSoul extends Card {

    public RemoveSoul() {
        setNeedsSpellTarget(true);
        setTargetFilter(new SpellTypeTargetFilter(Set.of(StackEntryType.CREATURE_SPELL)));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
