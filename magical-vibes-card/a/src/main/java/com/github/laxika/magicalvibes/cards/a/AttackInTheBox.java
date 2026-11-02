package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "242")
public class AttackInTheBox extends Card {

    public AttackInTheBox() {
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(
                        new BoostSelfEffect(4, 0),
                        new SacrificeSelfAtEndStepEffect()),
                "Have Attack-in-the-Box get +4/+0 until end of turn?"
        ));
    }
}
