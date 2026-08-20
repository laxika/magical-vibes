package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "236")
public class SpiritSummoning extends Card {

    public SpiritSummoning() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Spirit",
                3,
                2,
                CardColor.RED,
                Set.of(CardColor.RED, CardColor.WHITE),
                List.of(CardSubtype.SPIRIT)
        ));
    }
}
