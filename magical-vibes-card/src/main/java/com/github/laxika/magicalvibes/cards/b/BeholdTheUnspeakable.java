package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.v.VisionOfTheUnspeakable;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

@CardRegistration(set = "NEO", collectorNumber = "48")
public class BeholdTheUnspeakable extends Card {

    public BeholdTheUnspeakable() {
        setBackFaceCard(new VisionOfTheUnspeakable());

        addEffect(EffectSlot.SAGA_CHAPTER_I, new BoostAllCreaturesEffect(
                -2,
                0,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                GrantDuration.UNTIL_YOUR_NEXT_TURN));

        CardsInHandAtMost oneOrFewerCards = new CardsInHandAtMost(1);
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new ConditionalReplacementEffect(
                        oneOrFewerCards,
                        SequenceEffect.of(new ScryEffect(2), new DrawCardEffect(2)),
                        new DrawCardEffect(4)));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "VisionOfTheUnspeakable";
    }
}
