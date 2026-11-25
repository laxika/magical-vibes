package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.ControllerDrewAtLeastCardsThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "MKM", collectorNumber = "128")
public class Galvanize extends Card {

    public Galvanize() {
        addSpellEffect(DealDamageEffect.toTargetCreature(new FixedIfCondition(new ControllerDrewAtLeastCardsThisTurn(2), 5, 3)));
    }
}
