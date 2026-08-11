package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "INV", collectorNumber = "264")
public class Recoil extends Card {

    public Recoil() {
        target(new PermanentPredicateTargetFilter(new PermanentTruePredicate(),
                "Target must be a permanent"))
                .addEffect(EffectSlot.SPELL, new ReturnTargetPermanentToHandThenEffect(
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                        ThenEffectRecipient.TARGET_OWNER));
    }
}
