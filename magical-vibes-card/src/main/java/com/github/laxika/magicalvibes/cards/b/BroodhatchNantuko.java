package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONS", collectorNumber = "250")
public class BroodhatchNantuko extends Card {

    public BroodhatchNantuko() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new MayEffect(
                new CreateTokenEffect(new EventValue(), "Insect", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.INSECT), Set.of(), Set.of()),
                "Create that many 1/1 green Insect creature tokens?"));
        addMorph("{2}{G}");
    }
}
