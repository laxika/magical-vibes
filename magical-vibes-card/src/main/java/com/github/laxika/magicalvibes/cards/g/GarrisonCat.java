package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "14")
public class GarrisonCat extends Card {

    public GarrisonCat() {
        // When this creature dies, create a 1/1 white Human Soldier creature token.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                "Human Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of()));
    }
}
