package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
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
@CardRegistration(set = "TDC", collectorNumber = "154")
@CardRegistration(set = "OTC", collectorNumber = "99")
public class HaughtyDjinn extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(
            List.of(
                    new CardTypePredicate(CardType.INSTANT),
                    new CardTypePredicate(CardType.SORCERY)
            ));

    public HaughtyDjinn() {
        CardsInGraveyard instantAndSorceryCards =
                new CardsInGraveyard(INSTANT_OR_SORCERY, CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(instantAndSorceryCards, new Fixed(4)));
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                INSTANT_OR_SORCERY, 1, CostModificationScope.SELF));
    }
}
