package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "43")
@CardRegistration(set = "MOC", collectorNumber = "130")
public class VodalianWaveKnight extends Card {

    public VodalianWaveKnight() {
        // Whenever you draw a card, put a +1/+1 counter on each other Merfolk and/or Knight
        // you control.
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.MERFOLK, CardSubtype.KNIGHT)),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())))));
    }
}
