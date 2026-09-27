package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DiscardCardCastingCost;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "82")
@CardRegistration(set = "MSC", collectorNumber = "400")
public class DragonManReformedRobot extends Card {

    public DragonManReformedRobot() {
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new Max(
                        new GreatestManaValueAmongControlled(
                                new PermanentNotPredicate(new PermanentIsCreaturePredicate())),
                        new GreatestManaValueAmongCardsInGraveyard(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                                CountScope.CONTROLLER)),
                new Fixed(5)));
        addCastingOption(new GraveyardCast(List.of(new DiscardCardCastingCost())));
    }
}
