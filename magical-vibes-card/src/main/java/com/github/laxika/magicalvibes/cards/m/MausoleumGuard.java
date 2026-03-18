package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "20")
public class MausoleumGuard extends Card {

    public MausoleumGuard() {
        // When Mausoleum Guard dies, create two 1/1 white Spirit creature tokens with flying.
        addEffect(EffectSlot.ON_DEATH, new CreateCreatureTokenEffect(
                2, "Spirit", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of()
        ));
    }
}
