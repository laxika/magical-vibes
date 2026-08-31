package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.OnlyLandCreaturesCanAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "211")
public class BumiUnleashed extends Card {

    public BumiUnleashed() {
        target(TargetFilters.landYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EarthbendTargetLandEffect(4));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new AdditionalCombatPhaseEffect(1, new OnlyLandCreaturesCanAttackThisCombatEffect()));
    }
}
