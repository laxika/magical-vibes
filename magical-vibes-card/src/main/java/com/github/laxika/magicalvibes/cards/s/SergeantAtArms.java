package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.KickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "32")
public class SergeantAtArms extends Card {

    public SergeantAtArms() {
        // Kicker {2}{W}
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}{W}"));
        // When this creature enters, if it was kicked, create two 1/1 white Soldier creature tokens.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new KickedConditionalEffect(
                new CreateCreatureTokenEffect(2, "Soldier", 1, 1,
                        CardColor.WHITE, List.of(CardSubtype.SOLDIER),
                        Set.of(), Set.of())
        ));
    }
}
