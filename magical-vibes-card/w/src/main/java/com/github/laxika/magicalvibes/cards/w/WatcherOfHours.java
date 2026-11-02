package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.YouRemoveTimeCounterFromExiledCardTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "25")
@CardRegistration(set = "MKC", collectorNumber = "335")
public class WatcherOfHours extends Card {

    public WatcherOfHours() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(3));
        addTrigger(Triggers.timeCounterRemovedFromThisInExile(),
                new YouRemoveTimeCounterFromExiledCardTriggerEffect(new SurveilEffect(1)));
        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(),
                "Suspend 6—{1}{U}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(6));
    }
}
