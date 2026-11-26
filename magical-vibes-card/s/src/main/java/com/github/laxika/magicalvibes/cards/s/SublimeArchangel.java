package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M13", collectorNumber = "36")
@CardRegistration(set = "SLD", collectorNumber = "1381")
@CardRegistration(set = "UMA", collectorNumber = "38")
public class SublimeArchangel extends Card {

    public SublimeArchangel() {
        // Exalted: each instance triggers separately for a lone attacker (CR 702.83a).
        addTrigger(Triggers.creatureYouControlAttacks(),
                TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                BoostEffect.target(1, 1));
        // Other creatures you control have exalted; each granted instance is keyword-driven.
        addStaticEffect(new GrantKeywordEffect(Keyword.EXALTED, Subjects.otherCreaturesYouControl()));
    }
}
