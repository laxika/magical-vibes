package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.OtherCreaturesCantAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ARB", collectorNumber = "99")
public class IntimidationBolt extends Card {

    public IntimidationBolt() {
        // Deals 3 damage to target creature. Other creatures can't attack this turn (only the targeted
        // creature — if it survives — may attack; the rider reads this spell's target at resolution).
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(3))
                .addSpellEffect(new OtherCreaturesCantAttackThisTurnEffect());
    }
}
