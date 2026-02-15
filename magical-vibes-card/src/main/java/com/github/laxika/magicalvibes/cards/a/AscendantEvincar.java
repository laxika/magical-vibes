package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostNonColorCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostOtherCreaturesByColorEffect;

import java.util.List;
import java.util.Set;

public class AscendantEvincar extends Card {

    public AscendantEvincar() {
        super("Ascendant Evincar", CardType.CREATURE, "{4}{B}{B}", CardColor.BLACK);

        setSupertypes(Set.of(CardSupertype.LEGENDARY));
        setSubtypes(List.of(CardSubtype.PHYREXIAN, CardSubtype.VAMPIRE, CardSubtype.NOBLE));
        setCardText("Flying\nOther black creatures get +1/+1.\nNonblack creatures get -1/-1.");
        setKeywords(Set.of(Keyword.FLYING));
        setPower(3);
        setToughness(3);
        addEffect(EffectSlot.STATIC, new BoostOtherCreaturesByColorEffect(CardColor.BLACK, 1, 1));
        addEffect(EffectSlot.STATIC, new BoostNonColorCreaturesEffect(CardColor.BLACK, -1, -1));
    }
}
