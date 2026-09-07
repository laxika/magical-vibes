package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.WinnowingForces;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostControlledCountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "236")
public class InvasionOfLorwyn extends Card {

    public InvasionOfLorwyn() {
        setBackFaceCard(new WinnowingForces());

        PermanentAllOfPredicate targetFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ELF)),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                new PermanentPowerAtMostControlledCountPredicate(new PermanentIsLandPredicate())
        ));
        target(new PermanentPredicateTargetFilter(
                targetFilter,
                "Target must be a non-Elf creature an opponent controls with power less than or equal to the number of lands you control"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect(targetFilter));
    }

    @Override
    public String getBackFaceClassName() {
        return "WinnowingForces";
    }
}
