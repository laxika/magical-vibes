package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "102")
public class RotFarmMortipede extends Card {

    public RotFarmMortipede() {
        addTrigger(new TriggerCondition(Triggers.leavesGraveyard(Triggers.NONTOKEN_CREATURE, ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE,
                SequenceEffect.of(
                        BoostEffect.self(1, 0),
                        new GrantKeywordEffect(Keyword.MENACE, Subjects.source()),
                        new GrantKeywordEffect(Keyword.LIFELINK, Subjects.source())));
    }
}
