package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "115")
@CardRegistration(set = "9ED", collectorNumber = "103")
@CardRegistration(set = "8ED", collectorNumber = "107")
@CardRegistration(set = "7ED", collectorNumber = "104")
public class ThievingMagpie extends Card {

    public ThievingMagpie() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect());
    }
}
