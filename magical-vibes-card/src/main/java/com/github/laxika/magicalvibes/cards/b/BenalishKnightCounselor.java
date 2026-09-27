package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalPlusOnePlusOneCounterToNextCreatureSpellEffect;

@CardRegistration(set = "YDMU", collectorNumber = "1")
public class BenalishKnightCounselor extends Card {

    public BenalishKnightCounselor() {
        // Enlist is loaded from Scryfall and handled during attacker declaration.
        addEffect(EffectSlot.STATIC, new GrantAdditionalPlusOnePlusOneCounterToNextCreatureSpellEffect());
    }
}
