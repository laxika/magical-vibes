package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RollDiceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLZ", collectorNumber = "100")
@CardRegistration(set = "SLZ", collectorNumber = "221")
@CardRegistration(set = "SLZ", collectorNumber = "342")
public class ClownCar extends Card {

    public ClownCar() {
        CreateTokenEffect clownRobot = new CreateTokenEffect(
                1, "Clown Robot", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.CLOWN, CardSubtype.ROBOT), Set.of(), Set.of(CardType.ARTIFACT));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RollDiceEffect(
                new XValue(), 6, clownRobot,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
