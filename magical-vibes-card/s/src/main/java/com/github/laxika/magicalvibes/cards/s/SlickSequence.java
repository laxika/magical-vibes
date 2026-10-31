package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "OTJ", collectorNumber = "233")
public class SlickSequence extends Card {

    public SlickSequence() {
        addSpellEffect(new DealDamageToAnyTargetEffect(2));
        addSpellEffect(new ConditionalEffect(
                new ControllerCastAnotherSpellThisTurn(new CardTruePredicate()),
                new DrawCardEffect()));
    }
}
