package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterTypeOnEnterEffect;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "101")
public class TizerusCharger extends Card {

    public TizerusCharger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCounterTypeOnEnterEffect(CounterType.PLUS_ONE_PLUS_ONE, CounterType.FLYING));
        addCastingOption(new GraveyardCast(null, "{4}{B}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 5)), null, false, false, true));
    }
}
