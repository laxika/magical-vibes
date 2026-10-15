package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

public class LeylineSurge extends Card {

    public LeylineSurge() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new PutCardToBattlefieldEffect(new CardIsPermanentPredicate(), "permanent"),
                "Put a permanent card from your hand onto the battlefield?"));
    }
}
