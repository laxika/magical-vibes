package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

public class Afflict extends Card {

    public Afflict() {
        super("Afflict", CardType.INSTANT, "{2}{B}", CardColor.BLACK);

        setCardText("Target creature gets -1/-1 until end of turn.\nDraw a card.");
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-1, -1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
