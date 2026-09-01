package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "129")
public class CarelessCelebrant extends Card {

    public CarelessCelebrant() {
        PermanentPredicate creatureOrPlaneswalkerAnOpponentControls = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                )),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));

        target(new PermanentPredicateTargetFilter(
                creatureOrPlaneswalkerAnOpponentControls,
                "Target must be a creature or planeswalker an opponent controls"
        )).addEffect(EffectSlot.ON_DEATH,
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(2, creatureOrPlaneswalkerAnOpponentControls));
    }
}
