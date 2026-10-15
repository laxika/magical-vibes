package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "25")
public class SandsteppeOutcast extends Card {

    public SandsteppeOutcast() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on this creature",
                        new PutCountersOnSourceEffect(1, 1, 1)
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 1/1 white Spirit creature token with flying",
                        CreateTokenEffect.whiteSpirit(1)
                )
        )));
    }
}
