package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DestroyBlockedCreatureAndSelfEffect;

import java.util.List;

public class LoyalSentry extends Card {

    public LoyalSentry() {
        super("Loyal Sentry", CardType.CREATURE, "{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER));
        setCardText("When Loyal Sentry blocks a creature, destroy that creature and Loyal Sentry.");
        setPower(1);
        setToughness(1);
        addEffect(EffectSlot.ON_BLOCK, new DestroyBlockedCreatureAndSelfEffect());
    }
}
