package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;

@CardRegistration(set = "ORI", collectorNumber = "52")
public class DeepSeaTerror extends Card {

    public DeepSeaTerror() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new GraveyardCardThreshold(7, null),
                "there are seven or more cards in your graveyard"
        ));
    }
}
