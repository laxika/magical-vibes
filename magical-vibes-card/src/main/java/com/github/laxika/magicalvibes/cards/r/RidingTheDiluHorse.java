package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BuffTargetCreatureIndefinitelyEffect;

import java.util.Set;

@CardRegistration(set = "PTK", collectorNumber = "144")
public class RidingTheDiluHorse extends Card {

    public RidingTheDiluHorse() {
        // Target creature gets +2/+2 and gains horsemanship. (This effect lasts indefinitely.)
        addEffect(EffectSlot.SPELL, new BuffTargetCreatureIndefinitelyEffect(2, 2, Set.of(Keyword.HORSEMANSHIP)));
    }
}
