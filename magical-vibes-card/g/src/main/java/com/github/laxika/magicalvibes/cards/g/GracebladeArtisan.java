package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "DTK", collectorNumber = "20")
public class GracebladeArtisan extends Card {

    public GracebladeArtisan() {
        Scaled twicePerAura = new Scaled(new AttachmentsOnSource(true, false), 2);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(twicePerAura, twicePerAura));
    }
}
