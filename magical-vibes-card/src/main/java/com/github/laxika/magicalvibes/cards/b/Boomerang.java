package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;

public class Boomerang extends Card {

    public Boomerang() {
        super("Boomerang", CardType.INSTANT, "{U}{U}", CardColor.BLUE);

        setCardText("Return target permanent to its owner's hand.");
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new ReturnTargetPermanentToHandEffect());
    }
}
