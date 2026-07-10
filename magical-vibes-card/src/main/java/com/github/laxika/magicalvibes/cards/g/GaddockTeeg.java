package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.NoncreatureSpellsCantBeCastEffect;

@CardRegistration(set = "LRW", collectorNumber = "248")
public class GaddockTeeg extends Card {

    public GaddockTeeg() {
        // Noncreature spells with mana value 4 or greater can't be cast.
        // Noncreature spells with {X} in their mana costs can't be cast.
        addEffect(EffectSlot.STATIC, new NoncreatureSpellsCantBeCastEffect(4, true));
    }
}
