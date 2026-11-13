package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExertAsItAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "133")
@CardRegistration(set = "AKR", collectorNumber = "214")
public class RhonassStalwart extends Card {

    public RhonassStalwart() {
        addStaticEffect(new ExertAsItAttacksEffect());
        addTrigger(Triggers.exertedAsItAttacks(), SequenceEffect.of(
                                new BoostSelfEffect(1, 1),
                                new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                                new PermanentPowerAtLeastPredicate(3),
                                "creatures with power 3 or greater",
                                true)));
    }
}
