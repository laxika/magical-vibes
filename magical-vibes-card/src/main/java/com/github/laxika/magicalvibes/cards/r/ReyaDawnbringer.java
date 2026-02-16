package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureFromGraveyardToBattlefieldEffect;

public class ReyaDawnbringer extends Card {

    public ReyaDawnbringer() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(new ReturnCreatureFromGraveyardToBattlefieldEffect(), "Return a creature from your graveyard to the battlefield?"));
    }
}
