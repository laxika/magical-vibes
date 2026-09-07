package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.AdditionalMillForOpponentsEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "85")
@CardRegistration(set = "FIN", collectorNumber = "333")
public class TheWaterCrystal extends Card {

    public TheWaterCrystal() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardColorPredicate(CardColor.BLUE), 1, CostModificationScope.SELF));
        addEffect(EffectSlot.STATIC, new AdditionalMillForOpponentsEffect(4));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{U}{U}",
                List.of(new MillEffect(new CardsInHand(CountScope.CONTROLLER), MillRecipient.EACH_OPPONENT)),
                "{4}{U}{U}, {T}: Each opponent mills cards equal to the number of cards in your hand."));
    }
}
