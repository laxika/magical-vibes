package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;

@CardRegistration(set = "CMM", collectorNumber = "30")
public class HeraldOfTheHost extends Card {

    public HeraldOfTheHost() {
        addEffect(EffectSlot.ON_ATTACK,
                new CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect(true));
    }
}
