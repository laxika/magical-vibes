package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerCastTwoOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "EOE", collectorNumber = "8")
public class BrightspearZealot extends Card {

    public BrightspearZealot() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerCastTwoOrMoreSpellsThisTurn(new CardTruePredicate()),
                new StaticBoostEffect(2, 0, GrantScope.SELF)));
    }
}
