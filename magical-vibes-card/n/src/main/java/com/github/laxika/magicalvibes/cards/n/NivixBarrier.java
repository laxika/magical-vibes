package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "66")
public class NivixBarrier extends Card {

    public NivixBarrier() {
        // "When this creature enters, target attacking creature gets -4/-0 until end of turn."
        // Flash and Defender are keywords auto-loaded from Scryfall.
        target(TargetFilters.attackingCreature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new BoostTargetCreatureEffect(-4, 0));
    }
}
