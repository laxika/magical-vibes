package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIC", collectorNumber = "29")
@CardRegistration(set = "MIC", collectorNumber = "67")
public class SigardianZealot extends Card {

    public SigardianZealot() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                ChoosePermanentsEffect.anyNumber(Subjects.allCreatures(), "any number of creatures")
                        .withDifferent(ChoosePermanentsEffect.Different.POWER),
                // X is found once, as the effect is applied (CR 608.2h), so each gets the same X.
                BoostEffect.each(Subjects.thisWay(StepKind.CHOOSE), new SourcePower(), new SourcePower()),
                new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.thisWay(StepKind.CHOOSE), Duration.UNTIL_END_OF_TURN));
    }
}
