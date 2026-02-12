package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetBlockingCreatureEffect;

import java.util.List;

public class Righteousness extends Card {

    public Righteousness() {
        super("Righteousness", CardType.INSTANT, "{W}", CardColor.WHITE);

        setCardText("Target blocking creature gets +7/+7 until end of turn.");
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new BoostTargetBlockingCreatureEffect(7, 7));
    }
}
