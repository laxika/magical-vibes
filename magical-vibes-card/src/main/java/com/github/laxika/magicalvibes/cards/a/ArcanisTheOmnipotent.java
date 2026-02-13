package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;

import java.util.List;
import java.util.Set;

public class ArcanisTheOmnipotent extends Card {

    public ArcanisTheOmnipotent() {
        super("Arcanis the Omnipotent", CardType.CREATURE, "{3}{U}{U}{U}", CardColor.BLUE);

        setSupertypes(Set.of(CardSupertype.LEGENDARY));
        setSubtypes(List.of(CardSubtype.WIZARD));
        setCardText("{T}: Draw three cards.\n{2}{U}{U}: Return Arcanis the Omnipotent to its owner's hand.");
        setPower(3);
        setToughness(4);
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DrawCardEffect(3)), false, "{T}: Draw three cards."));
        addActivatedAbility(new ActivatedAbility(false, "{2}{U}{U}", List.of(new ReturnSelfToHandEffect()), false, "{2}{U}{U}: Return Arcanis the Omnipotent to its owner's hand."));
    }
}
