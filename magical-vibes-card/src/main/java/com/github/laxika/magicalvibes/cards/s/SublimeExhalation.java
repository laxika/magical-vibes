package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "CMM", collectorNumber = "60")
@CardRegistration(set = "CMM", collectorNumber = "475")
public class SublimeExhalation extends Card {

    public SublimeExhalation() {
        // Undaunted: this spell costs {1} less to cast for each opponent.
        addEffect(EffectSlot.STATIC,
                new ReduceOwnCastCostEffect(new Sum(new PlayersInGame(), new Fixed(-1))));

        // Destroy all creatures.
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}
