package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "105")
public class WeaponcraftEnthusiast extends Card {

    public WeaponcraftEnthusiast() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put two +1/+1 counters on Weaponcraft Enthusiast",
                        new PutCountersOnSourceEffect(1, 1, 2)
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Create two 1/1 colorless Servo artifact creature tokens",
                        new CreateTokenEffect(2, "Servo", 1, 1, null,
                                List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT))
                )
        )));
    }
}
