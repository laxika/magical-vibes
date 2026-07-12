package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreaturesWithPowerGreaterThanAmountCantAttackEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "300")
public class EnsnaringBridge extends Card {

    public EnsnaringBridge() {
        // Creatures with power greater than the number of cards in your hand can't attack.
        addEffect(EffectSlot.STATIC, new CreaturesWithPowerGreaterThanAmountCantAttackEffect(
                new CardsInHand(CountScope.CONTROLLER)
        ));
    }
}
