package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

import java.util.List;

/**
 * Incited Rabble — back face of Town Gossipmonger.
 */
public class IncitedRabble extends Card {

    public IncitedRabble() {
        // This creature attacks each combat if able.
        addEffect(EffectSlot.STATIC, new MustAttackEffect());

        // {2}: This creature gets +1/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new BoostSelfEffect(1, 0)),
                "{2}: This creature gets +1/+0 until end of turn."
        ));
    }
}
