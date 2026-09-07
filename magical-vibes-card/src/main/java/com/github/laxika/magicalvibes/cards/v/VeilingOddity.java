package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCardSuspended;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MakeAllCreaturesUnblockableEffect;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "51")
public class VeilingOddity extends Card {

    public VeilingOddity() {
        // When the last time counter is removed from this card while it's exiled, creatures can't
        // be blocked this turn. The negated suspend condition identifies the last counter event.
        addEffect(EffectSlot.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE, new ConditionalEffect(
                new NotCondition(new SourceCardSuspended()), new MakeAllCreaturesUnblockableEffect()));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(),
                "Suspend 4\u2014{1}{U}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(4));
    }
}
