package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "DSK", collectorNumber = "234")
public class SkullsnapNuisance extends Card {

    public SkullsnapNuisance() {
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, new SurveilEffect(1));
        addEffect(EffectSlot.ON_ALLY_ROOM_FULLY_UNLOCKED, new SurveilEffect(1));
    }
}
