package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControllerIsNotStartingPlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "YDSK", collectorNumber = "27")
public class UnnaturalSummons extends Card {

    public UnnaturalSummons() {
        // If you weren't the starting player, this spell costs {1} less to cast.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerIsNotStartingPlayer(), new ReduceOwnCastCostEffect(new Fixed(1))));

        // Manifest dread.
        addEffect(EffectSlot.SPELL, ManifestDreadEffect.forController());
    }
}
