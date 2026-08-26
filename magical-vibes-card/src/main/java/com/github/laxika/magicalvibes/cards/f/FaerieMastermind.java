package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "58")
public class FaerieMastermind extends Card {

    public FaerieMastermind() {
        addEffect(EffectSlot.ON_OPPONENT_DRAWS,
                new NthCardDrawTriggerEffect(2, new DrawCardEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new EachPlayerDrawsCardEffect(1)),
                "{3}{U}: Each player draws a card."
        ));
    }
}
