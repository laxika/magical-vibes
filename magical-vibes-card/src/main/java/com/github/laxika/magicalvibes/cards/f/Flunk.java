package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STX", collectorNumber = "71")
public class Flunk extends Card {

    public Flunk() {
        // Target creature gets -X/-X until end of turn, where X is 7 minus the number of cards
        // in that creature's controller's hand.
        CardsInHand targetControllerHand = new CardsInHand(CountScope.TARGET_PLAYER);
        DynamicAmount amount = new Max(new Fixed(0), new Sum(new Fixed(7), new Scaled(targetControllerHand, -1)));
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new BoostTargetCreatureEffect(new Scaled(amount, -1), new Scaled(amount, -1)));
    }
}
