package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesFromHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C17", collectorNumber = "17")
public class KheruMindEater extends Card {

    public KheruMindEater() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                TargetPlayerExilesFromHandEffect.faceDown(1));
        addStaticEffect(new AllowCastFromCardsExiledWithSourceEffect(false));
    }
}
