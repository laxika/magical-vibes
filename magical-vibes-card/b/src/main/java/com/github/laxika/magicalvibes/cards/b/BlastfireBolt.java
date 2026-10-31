package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAttachmentsOnTargetCreatureEffect;

@CardRegistration(set = "M15", collectorNumber = "130")
public class BlastfireBolt extends Card {

    public BlastfireBolt() {
        addSpellEffect(new DealDamageToTargetCreatureEffect(5));
        addSpellEffect(new DestroyAttachmentsOnTargetCreatureEffect(false, true));
    }
}
