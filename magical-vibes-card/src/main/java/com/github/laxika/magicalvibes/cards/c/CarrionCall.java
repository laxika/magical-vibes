package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "115")
public class CarrionCall extends Card {

    public CarrionCall() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2,
                "Phyrexian Insect",
                1,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.INSECT),
                Set.of(Keyword.INFECT),
                Set.of()
        ));
    }
}
