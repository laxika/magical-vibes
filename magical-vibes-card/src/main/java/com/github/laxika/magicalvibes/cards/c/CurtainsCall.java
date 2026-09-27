package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CMM", collectorNumber = "146")
@CardRegistration(set = "CMM", collectorNumber = "506")
public class CurtainsCall extends Card {

    public CurtainsCall() {
        // Undaunted: this spell costs {1} less to cast for each opponent.
        addEffect(EffectSlot.STATIC,
                new ReduceOwnCastCostEffect(new Sum(new PlayersInGame(), new Fixed(-1))));

        target(TargetFilters.creature(), 2, 2)
                .addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
    }
}
