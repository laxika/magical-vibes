package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "31")
public class SamiteHerbalist extends Card {

    public SamiteHerbalist() {
        addTrigger(Triggers.permanentYouControlBecomesTapped(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsSourceCardPredicate(),
                        new SequenceEffect(List.of(
                                new GainLifeEffect(1),
                                new ScryEffect(1)
                        ))));
    }
}
