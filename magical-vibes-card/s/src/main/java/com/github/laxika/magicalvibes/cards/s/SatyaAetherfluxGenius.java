package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentUnlessPayEnergyEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "3")
@CardRegistration(set = "M3C", collectorNumber = "15")
@CardRegistration(set = "M3C", collectorNumber = "23")
@CardRegistration(set = "M3C", collectorNumber = "31")
@CardRegistration(set = "M3C", collectorNumber = "142")
@CardRegistration(set = "M3C", collectorNumber = "146")
@CardRegistration(set = "M3C", collectorNumber = "150")
public class SatyaAetherfluxGenius extends Card {

    public SatyaAetherfluxGenius() {
        PermanentPredicate targetFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())
        ));

        target(new ControlledPermanentPredicateTargetFilter(
                targetFilter,
                "Target must be another nontoken creature you control"
        ), 0, 1).addEffect(EffectSlot.ON_ATTACK,
                new CreateTokenCopyOfTargetPermanentUnlessPayEnergyEqualToManaValueEffect(
                        new CreateTokenCopyOfTargetPermanentEffect(false, false, false, true)));
        addEffect(EffectSlot.ON_ATTACK, new EnergyCountersEffect(2));
    }
}
