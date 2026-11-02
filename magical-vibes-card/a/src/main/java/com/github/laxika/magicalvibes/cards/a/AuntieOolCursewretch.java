package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessBlightsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECC", collectorNumber = "2")
public class AuntieOolCursewretch extends Card {

    public AuntieOolCursewretch() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessBlightsEffect(2));

        TargetPermanentMatches controlsAffectedCreature = new TargetPermanentMatches(
                new PermanentControlledBySourceControllerPredicate());
        addTrigger(Triggers.minusOneCountersPutOnCreature(),
                new ConditionalEffect(controlsAffectedCreature, new DrawCardEffect()));
        addTrigger(Triggers.minusOneCountersPutOnCreature(),
                new ConditionalEffect(new NotCondition(controlsAffectedCreature),
                        new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER)));
    }
}
