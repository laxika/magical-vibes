package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "209")
public class SnapdaxApexOfTheHunt extends Card {

    public SnapdaxApexOfTheHunt() {
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
        )).addEffect(EffectSlot.ON_SELF_MUTATES,
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(4, creatureOrPlaneswalkerAnOpponentControls))
                .addEffect(EffectSlot.ON_SELF_MUTATES, new GainLifeEffect(4));
    }
}
