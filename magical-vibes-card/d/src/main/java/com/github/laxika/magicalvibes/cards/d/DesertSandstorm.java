package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "PTK", collectorNumber = "107")
public class DesertSandstorm extends Card {

    public DesertSandstorm() {
        // Desert Sandstorm deals 1 damage to each creature.
        addEffect(EffectSlot.SPELL, new MassDamageEffect(1));
    }
}
