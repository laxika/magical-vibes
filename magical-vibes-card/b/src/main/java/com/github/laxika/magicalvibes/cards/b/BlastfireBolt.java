package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAttachmentsOnTargetCreatureEffect;

@CardRegistration(set = "M15", collectorNumber = "130")
public class BlastfireBolt extends Card {

    public BlastfireBolt() {
        addSpellEffect(DealDamageEffect.toTargetCreature(5));
        addSpellEffect(new DestroyAttachmentsOnTargetCreatureEffect(false, true));
    }
}
