package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;

import java.util.List;

public class ArcanisTheOmnipotent extends Card {

    public ArcanisTheOmnipotent() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DrawCardEffect(3)), false, "{T}: Draw three cards."));
        addActivatedAbility(new ActivatedAbility(false, "{2}{U}{U}", List.of(new ReturnSelfToHandEffect()), false, "{2}{U}{U}: Return Arcanis the Omnipotent to its owner's hand."));
    }
}
