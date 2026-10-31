package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockPerEquipmentEffect;

@CardRegistration(set = "MBS", collectorNumber = "9")
public class KembasLegion extends Card {

    public KembasLegion() {
        addStaticEffect(new GrantAdditionalBlockPerEquipmentEffect());
    }
}
