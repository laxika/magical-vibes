package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;

@CardRegistration(set = "WAR", collectorNumber = "141")
public class RagingKronch extends Card {

    public RagingKronch() {
        addStaticEffect(new CantAttackOrBlockAloneEffect(false));
    }
}
