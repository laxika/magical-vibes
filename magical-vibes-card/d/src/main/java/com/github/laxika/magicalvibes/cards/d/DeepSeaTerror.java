package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;

@CardRegistration(set = "ORI", collectorNumber = "52")
public class DeepSeaTerror extends Card {

    public DeepSeaTerror() {
        addStaticEffect(CantAttackOrBlockUnlessEffect.attack(
                new GraveyardCardThreshold(7, null),
                "there are seven or more cards in your graveyard"
        ));
    }
}
