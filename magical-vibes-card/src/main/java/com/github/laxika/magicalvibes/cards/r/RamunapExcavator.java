package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;

@CardRegistration(set = "HOU", collectorNumber = "129")
public class RamunapExcavator extends Card {

    public RamunapExcavator() {
        // You may play lands from your graveyard.
        addEffect(EffectSlot.STATIC, new PlayLandsFromGraveyardEffect());
    }
}
