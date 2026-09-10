package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutOpponentOwnedExiledCardIntoGraveyardAndGainLifeEffect;

@CardRegistration(set = "BFZ", collectorNumber = "12")
public class RuinProcessor extends Card {

    public RuinProcessor() {
        addEffect(EffectSlot.ON_SELF_CAST,
                new PutOpponentOwnedExiledCardIntoGraveyardAndGainLifeEffect(5));
    }
}
