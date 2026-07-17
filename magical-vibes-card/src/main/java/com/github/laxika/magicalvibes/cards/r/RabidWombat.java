package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "5ED", collectorNumber = "319")
public class RabidWombat extends Card {

    public RabidWombat() {
        // Rabid Wombat gets +2/+2 for each Aura attached to it. (Vigilance is auto-loaded.)
        Scaled twicePerAura = new Scaled(new AttachmentsOnSource(true, false), 2);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(twicePerAura, twicePerAura));
    }
}
