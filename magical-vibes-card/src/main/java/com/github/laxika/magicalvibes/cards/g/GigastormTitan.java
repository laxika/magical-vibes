package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "EOE", collectorNumber = "57")
public class GigastormTitan extends Card {

    public GigastormTitan() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerCastAnotherSpellThisTurn(new CardTruePredicate()),
                new ReduceOwnCastCostEffect(new Fixed(3))));
    }
}
