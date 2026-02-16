package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CreatureColorTargetFilter;

import java.util.Set;

public class Deathmark extends Card {

    public Deathmark() {
        setNeedsTarget(true);
        setTargetFilter(new CreatureColorTargetFilter(Set.of(CardColor.GREEN, CardColor.WHITE)));
        addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(Set.of(CardType.CREATURE)));
    }
}
