package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.EndTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "581")
public class TheDrumMiningFacility extends Card {

    public TheDrumMiningFacility() {
        addTrigger(Triggers.youRollOneOrMoreDice(), SequenceEffect.of(
                BoostEffect.creaturesYouControl(1, 1),
                new GrantKeywordEffect(Keyword.HASTE, Subjects.otherCreaturesYouControl())));
        addTrigger(Triggers.chaosEnsues(), new EndTurnEffect());
    }
}
