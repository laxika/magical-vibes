package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromTargetGraveyardThenManifestEffect;

@CardRegistration(set = "FRF", collectorNumber = "70")
@CardRegistration(set = "C19", collectorNumber = "115")
@CardRegistration(set = "C16", collectorNumber = "111")
public class GhastlyConscription extends Card {

    public GhastlyConscription() {
        addSpellEffect(new ExileCreaturesFromTargetGraveyardThenManifestEffect());
    }
}
