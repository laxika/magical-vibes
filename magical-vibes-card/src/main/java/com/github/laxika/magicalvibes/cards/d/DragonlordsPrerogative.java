package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlledDragonAsCast;
import com.github.laxika.magicalvibes.model.condition.RevealCardFromHandCostPaid;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardFromHandCost;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "52")
public class DragonlordsPrerogative extends Card {

    public DragonlordsPrerogative() {
        addEffect(EffectSlot.SPELL, RevealCardFromHandCost.optional(
                new CardSubtypePredicate(CardSubtype.DRAGON), "Dragon"));
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect(new AnyOf(List.of(
                new RevealCardFromHandCostPaid(), new ControlledDragonAsCast()))));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(4));
    }
}
