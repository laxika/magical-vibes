package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;

@CardRegistration(set = "DSK", collectorNumber = "69")
public class ParanormalAnalyst extends Card {

    public ParanormalAnalyst() {
        addEffect(EffectSlot.ON_CONTROLLER_MANIFESTS_DREAD, new ReturnTriggeringCardToOwnerHandEffect());
    }
}
