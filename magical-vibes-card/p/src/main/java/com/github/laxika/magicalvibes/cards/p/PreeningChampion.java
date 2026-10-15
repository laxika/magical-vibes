package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "73")
public class PreeningChampion extends Card {

    public PreeningChampion() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Elemental",
                1,
                1,
                CardColor.BLUE,
                Set.of(CardColor.BLUE, CardColor.RED),
                List.of(CardSubtype.ELEMENTAL)
        ));
    }
}
