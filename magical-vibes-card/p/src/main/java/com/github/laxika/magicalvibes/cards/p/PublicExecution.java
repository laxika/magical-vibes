package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTargetCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M13", collectorNumber = "105")
public class PublicExecution extends Card {

    public PublicExecution() {
        // "Each other creature that player controls": the targeted creature is excluded even when it
        // survives destruction (regeneration), so the rider filters out the target card by identity.
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                        new BoostAllCreaturesEffect(new Fixed(-2), new Fixed(0),
                                new PermanentNotPredicate(new PermanentIsTargetCardPredicate()),
                                EachPermanentScope.TARGET_PLAYER),
                        ThenEffectRecipient.TARGET_CONTROLLER_AS_TARGET));
    }
}
