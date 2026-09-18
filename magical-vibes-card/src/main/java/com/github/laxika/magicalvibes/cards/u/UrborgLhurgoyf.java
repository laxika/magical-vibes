package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "186")
public class UrborgLhurgoyf extends Card {

    public UrborgLhurgoyf() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(
                List.of("{U}", "{B}"), true, 1));

        Sum kicks = new Sum(
                new RepeatedAdditionalCostCount("{U}"),
                new RepeatedAdditionalCostCount("{B}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MillEffect(new Scaled(kicks, 3), MillRecipient.CONTROLLER));

        CardsInGraveyard creatureCards = new CardsInGraveyard(
                new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                creatureCards, new Sum(creatureCards, new Fixed(1))));
    }
}
