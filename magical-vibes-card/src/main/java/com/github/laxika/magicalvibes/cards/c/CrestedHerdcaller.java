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

@CardRegistration(set = "RIX", collectorNumber = "126")
public class CrestedHerdcaller extends Card {

    public CrestedHerdcaller() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Dinosaur",
                3,
                3,
                CardColor.GREEN,
                List.of(CardSubtype.DINOSAUR),
                Set.of(Keyword.TRAMPLE),
                Set.of()
        ));
    }
}
