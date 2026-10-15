package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllNonManaAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "45")
public class TownRazerTyrant extends Card {

    public TownRazerTyrant() {
        var nonbasicLandYouDoNotControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC)),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));

        target(new PermanentPredicateTargetFilter(
                nonbasicLandYouDoNotControl,
                "Target must be a nonbasic land you don't control"
        ))
        .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantStaticEffectToTargetEffect(
                        new LosesAllNonManaAbilitiesEffect(GrantScope.SELF)))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantEffectToTargetEffect(
                                EffectSlot.UPKEEP_TRIGGERED,
                                new ForcedCostOrElseEffect(
                                        new SacrificePermanentCost(
                                                new PermanentIsSourcePermanentPredicate(),
                                                "Sacrifice it", false),
                                        List.of(new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER)),
                                        true)));
    }
}
