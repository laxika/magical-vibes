package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIC", collectorNumber = "8")
@CardRegistration(set = "MIC", collectorNumber = "46")
public class SigardasVanguard extends Card {

    public SigardasVanguard() {
        CardEffect chooseDifferentPowers = ChoosePermanentsEffect.anyNumber(
                        Subjects.allCreatures(), "any number of creatures")
                .withDifferent(ChoosePermanentsEffect.Different.POWER);
        CardEffect grantDoubleStrike = new GrantKeywordEffect(
                Keyword.DOUBLE_STRIKE, Subjects.thisWay(StepKind.CHOOSE), Duration.UNTIL_END_OF_TURN);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                chooseDifferentPowers, grantDoubleStrike);
        addTrigger(Triggers.attacks(),
                chooseDifferentPowers, grantDoubleStrike);
    }
}
