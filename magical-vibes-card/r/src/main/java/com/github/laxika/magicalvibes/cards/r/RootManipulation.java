package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOS", collectorNumber = "222")
public class RootManipulation extends Card {

    public RootManipulation() {
        // Until end of turn, creatures you control get +2/+2 and gain menace.
        addSpellEffect(BoostEffect.creaturesYouControl(2, 2));
        addSpellEffect(new GrantKeywordEffect(Keyword.MENACE, Subjects.otherCreaturesYouControl()));

        // Until end of turn, creatures you control gain
        // "Whenever this creature attacks, you gain 1 life."
        addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(
                new TriggeredAbility(Triggers.attacks(), new GainLifeEffect(1)), Subjects.creaturesYouControl()));
    }
}
