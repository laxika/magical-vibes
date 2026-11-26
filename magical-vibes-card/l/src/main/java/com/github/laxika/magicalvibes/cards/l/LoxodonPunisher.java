package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;

@CardRegistration(set = "MRD", collectorNumber = "14")
public class LoxodonPunisher extends Card {

    public LoxodonPunisher() {
        Scaled twicePerEquipment = new Scaled(new AttachmentsOnSource(false, true), 2);
        addStaticEffect(BoostEffect.self(twicePerEquipment, twicePerEquipment));
    }
}
