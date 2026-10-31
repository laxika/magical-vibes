package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForFirstMatchingSpellEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "430")
public class MelekReforgedResearcher extends Card {

    public MelekReforgedResearcher() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)
        ));
        CardsInGraveyard instantsAndSorceries = new CardsInGraveyard(instantOrSorcery, CountScope.CONTROLLER);
        Scaled twiceTheGraveyardCount = new Scaled(instantsAndSorceries, 2);

        addStaticEffect(new SetPowerToughnessToAmountEffect(
                twiceTheGraveyardCount, twiceTheGraveyardCount));
        addStaticEffect(new ReduceCastCostForFirstMatchingSpellEachTurnEffect(
                instantOrSorcery, 3));
    }
}
