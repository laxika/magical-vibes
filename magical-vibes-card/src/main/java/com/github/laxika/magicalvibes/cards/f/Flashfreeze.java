package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.SpellColorTargetFilter;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "84")
public class Flashfreeze extends Card {

    public Flashfreeze() {
        setNeedsSpellTarget(true);
        setTargetFilter(new SpellColorTargetFilter(Set.of(CardColor.RED, CardColor.GREEN)));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
