package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "C14", collectorNumber = "40")
public class VolcanicOffering extends Card {

    public VolcanicOffering() {
        PermanentAllOfPredicate nonbasicLand = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC)),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));
        PermanentAllOfPredicate opposingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));

        target(new PermanentPredicateTargetFilter(nonbasicLand,
                "Target must be a nonbasic land you don't control"))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        target(new PermanentPredicateTargetFilter(nonbasicLand,
                "Target must be a nonbasic land you don't control"))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        target(new PermanentPredicateTargetFilter(opposingCreature,
                "Target must be a creature you don't control"))
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(7));
        target(new PermanentPredicateTargetFilter(opposingCreature,
                "Target must be a creature you don't control"))
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(7));

        setAllowSharedTargets(true);
        setOpponentChosenSpellTargetIndices(List.of(1, 3));
    }
}
