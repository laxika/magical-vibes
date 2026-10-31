package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "APC", collectorNumber = "116")
@CardRegistration(set = "DDJ", collectorNumber = "27")
@CardRegistration(set = "VMA", collectorNumber = "257")
@CardRegistration(set = "2X2", collectorNumber = "265")
@CardRegistration(set = "CMD", collectorNumber = "219")
@CardRegistration(set = "C15", collectorNumber = "231")
@CardRegistration(set = "C20", collectorNumber = "227")
public class PropheticBolt extends Card {

    public PropheticBolt() {
        addSpellEffect(new DealDamageToAnyTargetEffect(4));
        addSpellEffect(LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(4)));
    }
}
