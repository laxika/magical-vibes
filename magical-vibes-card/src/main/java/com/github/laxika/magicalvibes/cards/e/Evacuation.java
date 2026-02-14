package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;

import java.util.Set;

public class Evacuation extends Card {

    public Evacuation() {
        super("Evacuation", CardType.INSTANT, "{3}{U}{U}", CardColor.BLUE);

        setCardText("Return all creatures to their owners' hands.");
        addEffect(EffectSlot.SPELL, new ReturnCreaturesToOwnersHandEffect(Set.of()));
    }
}
