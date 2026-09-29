package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "567")
public class BrawnAmadeusCho extends Card {

    public BrawnAmadeusCho() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G/U}",
                List.of(new PutCountersOnSelfEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new CardsInHand(CountScope.CONTROLLER))),
                "Power-up — {4}{G/U}: Put a +1/+1 counter on Brawn for each card in your hand. Activate each power-up "
                        + "ability only once. Reduce the cost by his mana cost if he entered this turn."
        ).withPowerUp());
    }
}
