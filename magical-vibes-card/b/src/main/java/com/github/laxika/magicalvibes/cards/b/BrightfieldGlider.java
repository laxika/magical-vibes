package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsSaddled;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SaddleCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "4")
public class BrightfieldGlider extends Card {

    public BrightfieldGlider() {
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withEventCondition(new SourceIsSaddled()),
                SequenceEffect.of(BoostEffect.self(1, 2), new GrantKeywordEffect(Keyword.FLYING, Subjects.source())));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SaddleCost(3), new BecomeSaddledUntilEndOfTurnEffect(Subjects.source())),
                "Saddle 3",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
