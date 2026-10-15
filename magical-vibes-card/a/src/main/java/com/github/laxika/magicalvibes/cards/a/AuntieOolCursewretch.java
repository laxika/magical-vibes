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

@CardRegistration(set = "ECC", collectorNumber = "2")
public class AuntieOolCursewretch extends Card {

    public AuntieOolCursewretch() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessBlightsEffect(2));

        TargetPermanentMatches controlsAffectedCreature = new TargetPermanentMatches(
                new PermanentControlledBySourceControllerPredicate());
        addEffect(EffectSlot.ON_MINUS_ONE_MINUS_ONE_COUNTERS_PUT_ON_CREATURE,
                new ConditionalEffect(controlsAffectedCreature, new DrawCardEffect()));
        addEffect(EffectSlot.ON_MINUS_ONE_MINUS_ONE_COUNTERS_PUT_ON_CREATURE,
                new ConditionalEffect(new NotCondition(controlsAffectedCreature),
                        new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER)));
    }
}
