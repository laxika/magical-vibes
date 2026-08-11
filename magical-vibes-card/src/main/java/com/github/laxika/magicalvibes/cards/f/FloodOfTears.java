package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "M20", collectorNumber = "59")
public class FloodOfTears extends Card {

    public FloodOfTears() {
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.allPermanentsMatchingThen(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                4,
                new MayEffect(
                        new PutCardToBattlefieldEffect(new CardIsPermanentPredicate(), "permanent"),
                        "Put a permanent card from your hand onto the battlefield?")));
    }
}
