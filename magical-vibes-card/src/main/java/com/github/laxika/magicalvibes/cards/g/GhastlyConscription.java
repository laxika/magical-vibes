package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromTargetGraveyardThenManifestEffect;

@CardRegistration(set = "FRF", collectorNumber = "70")
public class GhastlyConscription extends Card {

    public GhastlyConscription() {
        addEffect(EffectSlot.SPELL, new ExileCreaturesFromTargetGraveyardThenManifestEffect());
    }
}
