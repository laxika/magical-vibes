package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "52")
public class HaughtyDjinn extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(
            List.of(
                    new CardTypePredicate(CardType.INSTANT),
                    new CardTypePredicate(CardType.SORCERY)
            ));

    public HaughtyDjinn() {
        CardsInGraveyard instantAndSorceryCards =
                new CardsInGraveyard(INSTANT_OR_SORCERY, CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(instantAndSorceryCards, new Fixed(4)));
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                INSTANT_OR_SORCERY, 1, CostModificationScope.SELF));
    }
}
