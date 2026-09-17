package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "197")
@CardRegistration(set = "MM3", collectorNumber = "189")
@CardRegistration(set = "MB1", collectorNumber = "189")
@CardRegistration(set = "2X2", collectorNumber = "278")
public class SproutingThrinax extends Card {

    public SproutingThrinax() {
        // When this creature dies, create three 1/1 green Saproling creature tokens.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                3, "Saproling", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.SAPROLING), Set.of(), Set.of()
        ));
    }
}
