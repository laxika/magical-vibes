package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.OnlyLandCreaturesCanAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "211")
public class BumiUnleashed extends Card {

    public BumiUnleashed() {
        target(TargetFilters.landYouControl())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new EarthbendTargetLandEffect(4));

        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new AdditionalCombatPhaseEffect(1, new OnlyLandCreaturesCanAttackThisCombatEffect()));
    }
}
