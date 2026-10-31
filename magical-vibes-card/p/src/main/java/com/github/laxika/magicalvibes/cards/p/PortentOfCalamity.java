package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsForEachCardTypeMayExileEffect;

@CardRegistration(set = "BLB", collectorNumber = "66")
public class PortentOfCalamity extends Card {

    public PortentOfCalamity() {
        addSpellEffect(new RevealTopCardsForEachCardTypeMayExileEffect(new XValue()));
    }
}
