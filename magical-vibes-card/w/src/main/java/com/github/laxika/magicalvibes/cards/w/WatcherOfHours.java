package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "25")
@CardRegistration(set = "MKC", collectorNumber = "335")
public class WatcherOfHours extends Card {

    public WatcherOfHours() {
        addEffect(EffectSlot.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE, new SurveilEffect(1));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(),
                "Suspend 6—{1}{U}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(6));
    }
}
