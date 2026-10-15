package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "46")
public class BoomerangBasics extends Card {

    public BoomerangBasics() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, new ReturnTargetPermanentToHandThenEffect(
                        new DrawCardEffect(1),
                        ThenEffectRecipient.CONTROLLER,
                        null,
                        new TargetPermanentMatches(new PermanentControlledBySourceControllerPredicate())));
    }
}
