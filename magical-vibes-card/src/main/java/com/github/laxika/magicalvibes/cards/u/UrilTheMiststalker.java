package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "ARB", collectorNumber = "124")
public class UrilTheMiststalker extends Card {

    public UrilTheMiststalker() {
        // Hexproof is auto-loaded from Scryfall.

        // Uril, the Miststalker gets +2/+2 for each Aura attached to it.
        Scaled twicePerAura = new Scaled(new AttachmentsOnSource(true, false), 2);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(twicePerAura, twicePerAura));
    }
}
