package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "PLS", collectorNumber = "78")
public class AmphibiousKavu extends Card {

    public AmphibiousKavu() {
        PermanentColorInPredicate blueOrBlack =
                new PermanentColorInPredicate(Set.of(CardColor.BLUE, CardColor.BLACK));
        addTrigger(Triggers.blocks().withFilter(blueOrBlack),
                TriggerOptions.ONE_OR_MORE,
                new BoostSelfEffect(3, 3));
        addTrigger(Triggers.becomesBlocked().withFilter(blueOrBlack),
                new BoostSelfEffect(3, 3));
    }
}
