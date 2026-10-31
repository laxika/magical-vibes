package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAttachmentsOnTargetCreatureEffect;

@CardRegistration(set = "SOM", collectorNumber = "106")
@CardRegistration(set = "M13", collectorNumber = "153")
@CardRegistration(set = "M21", collectorNumber = "168")
public class TurnToSlag extends Card {

    public TurnToSlag() {
        addSpellEffect(new DealDamageToTargetCreatureEffect(5));
        addSpellEffect(new DestroyAttachmentsOnTargetCreatureEffect(false, true));
    }
}
