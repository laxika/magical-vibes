package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CountersCantBePlacedEffect;

@CardRegistration(set = "HOU", collectorNumber = "22")
public class Solemnity extends Card {

    public Solemnity() {
        // "Players can't get counters. Counters can't be put on artifacts, creatures,
        // enchantments, or lands." — one global replacement lock queried at the counter chokepoints.
        addEffect(EffectSlot.STATIC, new CountersCantBePlacedEffect());
    }
}
