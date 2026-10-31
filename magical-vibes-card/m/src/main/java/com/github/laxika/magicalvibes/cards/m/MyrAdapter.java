package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "MRD", collectorNumber = "210")
public class MyrAdapter extends Card {

    public MyrAdapter() {
        AttachmentsOnSource onePerEquipment = new AttachmentsOnSource(false, true);
        addStaticEffect(new BoostSelfEffect(onePerEquipment, onePerEquipment));
    }
}
