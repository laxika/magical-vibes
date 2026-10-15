package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastSameNameCardFromGraveyardOnSpellCastEffect;

@CardRegistration(set = "SOI", collectorNumber = "163")
public class HarnessTheStorm extends Card {

    public HarnessTheStorm() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new CastSameNameCardFromGraveyardOnSpellCastEffect());
    }
}
