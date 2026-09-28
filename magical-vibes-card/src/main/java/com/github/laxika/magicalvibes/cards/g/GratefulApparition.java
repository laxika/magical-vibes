package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "WAR", collectorNumber = "17")
@CardRegistration(set = "FIC", collectorNumber = "244")
@CardRegistration(set = "NCC", collectorNumber = "202")
public class GratefulApparition extends Card {

    public GratefulApparition() {
        addEffect(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER,
                new ProliferateEffect());
    }
}
