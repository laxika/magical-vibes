package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackPlayerAlreadyAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SPG", collectorNumber = "33")
public class PortRazer extends Card {

    public PortRazer() {
        addEffect(EffectSlot.STATIC, new CantAttackPlayerAlreadyAttackedThisTurnEffect());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new AdditionalCombatPhaseEffect(1));
    }
}
