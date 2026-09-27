package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatedPermanentsAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.ScheduleCreatedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

@CardRegistration(set = "YDFT", collectorNumber = "14")
public class KariZevCrewOfTwo extends Card {

    public KariZevCrewOfTwo() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new ControlsPermanentCountAtMost(0, new PermanentAllOfPredicate(List.of(
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                        new PermanentHasSubtypePredicate(CardSubtype.MONKEY)))),
                SequenceEffect.of(
                        new ConjureCardToBattlefieldEffect("Ragavan, Nimble Pilferer"),
                        new MakeCreatedPermanentsAttackingEffect(true),
                        new ScheduleCreatedPermanentsEffect(DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP))));
    }
}
