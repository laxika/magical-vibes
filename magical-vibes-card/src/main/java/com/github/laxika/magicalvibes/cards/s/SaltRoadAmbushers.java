package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "198")
public class SaltRoadAmbushers extends Card {

    public SaltRoadAmbushers() {
        addMorph("{3}{G}{G}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new PutCountersOnTurnFaceUpEffect(1));
        addEffect(EffectSlot.ON_SELF_OR_ALLY_PERMANENT_TURNS_FACE_UP,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                                new PermanentIsCreaturePredicate())),
                        new PutCounterOnReferencedPermanentEffect(
                                PermanentReference.TRIGGERING,
                                CounterType.PLUS_ONE_PLUS_ONE,
                                new Fixed(2),
                                new PermanentIsCreaturePredicate())));
    }
}
