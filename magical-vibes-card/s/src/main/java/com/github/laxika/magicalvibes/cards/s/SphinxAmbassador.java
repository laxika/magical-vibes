package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SphinxAmbassadorEffect;

@CardRegistration(set = "M10", collectorNumber = "73")
public class SphinxAmbassador extends Card {

    public SphinxAmbassador() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new SphinxAmbassadorEffect());
    }
}
