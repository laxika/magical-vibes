package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "MRD", collectorNumber = "14")
public class LoxodonPunisher extends Card {

    public LoxodonPunisher() {
        Scaled twicePerEquipment = new Scaled(new AttachmentsOnSource(false, true), 2);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(twicePerEquipment, twicePerEquipment));
    }
}
