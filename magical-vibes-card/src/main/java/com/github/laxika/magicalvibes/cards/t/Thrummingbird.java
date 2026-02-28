package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "SOM", collectorNumber = "47")
public class Thrummingbird extends Card {

    public Thrummingbird() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ProliferateEffect());
    }
}
