package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GRN", collectorNumber = "15")
public class HuntedWitness extends Card {

    public HuntedWitness() {
        // When this creature dies, create a 1/1 white Soldier creature token with lifelink.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1, "Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.SOLDIER), Set.of(Keyword.LIFELINK), Set.of()));
    }
}
