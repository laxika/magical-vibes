package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "YDFT", collectorNumber = "19")
public class MitoticUltimus extends Card {

    public MitoticUltimus() {
        addEffect(EffectSlot.STATIC,
                new ReduceOwnCastCostEffect(new GreatestPowerAmongControlled()));
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new ConjureCardToBattlefieldEffect("Mitotic Slime"),
                new ConjureCardToBattlefieldEffect("Mitotic Slime")));
    }
}
