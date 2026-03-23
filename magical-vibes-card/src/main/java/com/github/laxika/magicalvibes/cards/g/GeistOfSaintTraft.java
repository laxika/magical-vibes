package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "213")
public class GeistOfSaintTraft extends Card {

    public GeistOfSaintTraft() {
        addEffect(EffectSlot.ON_ATTACK, new CreateTokenEffect(
                1, "Angel", 4, 4, CardColor.WHITE, List.of(CardSubtype.ANGEL),
                Set.of(Keyword.FLYING), true, true
        ));
    }
}
