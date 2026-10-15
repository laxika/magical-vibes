package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PestProblem extends Card {

    public PestProblem() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2, "Rat", 1, 1, CardColor.BLACK, List.of(CardSubtype.RAT),
                Set.of(), Set.of(), Map.of(EffectSlot.STATIC, new CantBlockEffect())));
    }
}
