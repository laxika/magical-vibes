package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

@CardRegistration(set = "MB1", collectorNumber = "27")
public class MemoryBank extends Card {

    public MemoryBank() {
        addSpellEffect(new DrawCardEffect());
        // Bank: "If you cast Memory Bank from your hand, exile it"; the rest of its reminder spans games of a match.
        addSpellEffect(new ConditionalEffect(new CastFromZone(Zone.HAND), ExileSpellEffect.immediately()));
    }
}
