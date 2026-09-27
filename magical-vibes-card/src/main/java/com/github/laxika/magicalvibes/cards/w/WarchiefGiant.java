package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;

@CardRegistration(set = "C15", collectorNumber = "31")
public class WarchiefGiant extends Card {

    public WarchiefGiant() {
        addEffect(EffectSlot.ON_ATTACK,
                CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect.myriad());
    }
}
