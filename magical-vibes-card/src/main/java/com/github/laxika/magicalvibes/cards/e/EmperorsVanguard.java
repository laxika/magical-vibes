package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;

@CardRegistration(set = "XLN", collectorNumber = "189")
public class EmperorsVanguard extends Card {

    public EmperorsVanguard() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ExploreEffect());
    }
}
