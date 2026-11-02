package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.v.VisionOfTheUnspeakable;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "48")
public class BeholdTheUnspeakable extends Card {

    public BeholdTheUnspeakable() {
        setBackFaceCard(new VisionOfTheUnspeakable());

        addTrigger(Triggers.chapter(1), new BoostAllCreaturesEffect(
                -2,
                0,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                GrantDuration.UNTIL_YOUR_NEXT_TURN));

        CardsInHandAtMost oneOrFewerCards = new CardsInHandAtMost(1);
        addTrigger(Triggers.chapter(2),
                new ConditionalReplacementEffect(
                        oneOrFewerCards,
                        SequenceEffect.of(new ScryEffect(2), new DrawCardEffect(2)),
                        new DrawCardEffect(4)));

        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "VisionOfTheUnspeakable";
    }
}
