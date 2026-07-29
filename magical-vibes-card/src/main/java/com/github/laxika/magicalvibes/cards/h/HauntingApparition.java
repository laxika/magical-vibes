package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "267")
public class HauntingApparition extends Card {

    public HauntingApparition() {
        // Power is 1 plus the number of green creature cards in the chosen opponent's graveyard
        // (single-opponent model: the chosen player is the controller's opponent). Toughness stays 2.
        CardsInGraveyard greenCreatures = new CardsInGraveyard(new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardColorPredicate(CardColor.GREEN)
        )), CountScope.OPPONENTS);
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(new Sum(new Fixed(1), greenCreatures), new Fixed(2)));
    }
}
