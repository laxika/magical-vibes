package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "102")
@CardRegistration(set = "MAR", collectorNumber = "16")
@CardRegistration(set = "SLZ", collectorNumber = "41")
@CardRegistration(set = "SLZ", collectorNumber = "162")
@CardRegistration(set = "SLZ", collectorNumber = "283")
@CardRegistration(set = "OMB", collectorNumber = "16")
@CardRegistration(set = "SOA", collectorNumber = "29")
@CardRegistration(set = "CMM", collectorNumber = "159")
public class FeedTheSwarm extends Card {

    public FeedTheSwarm() {
        PermanentAllOfPredicate targetFilter = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsEnchantmentPredicate()
                )),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));
        target(new PermanentPredicateTargetFilter(
                targetFilter,
                "Target must be a creature or enchantment an opponent controls."
        )).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                EventStat.MANA_VALUE,
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.CONTROLLER),
                ThenEffectRecipient.CONTROLLER
        ));
    }
}
