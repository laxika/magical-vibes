package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "58")
@CardRegistration(set = "MOM", collectorNumber = "352")
@CardRegistration(set = "SOC", collectorNumber = "114")
public class FaerieMastermind extends Card {

    public FaerieMastermind() {
        addTrigger(Triggers.opponentDrawsCard(),
                new NthCardDrawTriggerEffect(2, new DrawCardEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new EachPlayerDrawsCardEffect(1)),
                "{3}{U}: Each player draws a card."
        ));
    }
}
