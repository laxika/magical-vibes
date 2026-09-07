package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "116")
public class VoraciousVermin extends Card {

    public VoraciousVermin() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                1,
                "Rat",
                1,
                1,
                CardColor.BLACK,
                List.of(CardSubtype.RAT),
                Set.of(),
                Set.of(),
                Map.of(EffectSlot.STATIC, new CantBlockEffect())));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
