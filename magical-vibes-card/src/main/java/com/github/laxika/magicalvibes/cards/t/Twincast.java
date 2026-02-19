package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.filter.SpellTypeTargetFilter;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "120")
public class Twincast extends Card {

    public Twincast() {
        setNeedsSpellTarget(true);
        setTargetFilter(new SpellTypeTargetFilter(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)));
        addEffect(EffectSlot.SPELL, new CopySpellEffect());
    }
}
