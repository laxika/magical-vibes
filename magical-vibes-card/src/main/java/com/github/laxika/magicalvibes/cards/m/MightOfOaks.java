package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

import java.util.List;

public class MightOfOaks extends Card {

    public MightOfOaks() {
        super("Might of Oaks", CardType.INSTANT, "{3}{G}", CardColor.GREEN);

        setCardText("Target creature gets +7/+7 until end of turn.");
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(7, 7));
    }
}
