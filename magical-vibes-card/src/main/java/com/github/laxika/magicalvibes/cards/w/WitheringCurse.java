package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SOS", collectorNumber = "105")
public class WitheringCurse extends Card {

    public WitheringCurse() {
        // All creatures get -2/-2 until end of turn. Infusion — if you gained life this turn,
        // destroy all creatures instead.
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new GainedLifeThisTurn(),
                new BoostAllCreaturesEffect(-2, -2),
                new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate())));
    }
}
