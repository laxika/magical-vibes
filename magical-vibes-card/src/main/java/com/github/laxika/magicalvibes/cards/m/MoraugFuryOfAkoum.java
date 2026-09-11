package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerMainPhase;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.BoostByAttackCountEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ZNR", collectorNumber = "150")
public class MoraugFuryOfAkoum extends Card {

    public MoraugFuryOfAkoum() {
        addEffect(EffectSlot.STATIC,
                new BoostByAttackCountEffect(1, 0, GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new ConditionalEffect(new ControllerMainPhase(),
                        new AdditionalCombatMainPhaseEffect(1,
                                new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                                        new PermanentIsCreaturePredicate()))));
    }
}
