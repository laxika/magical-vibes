package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "106")
public class MODOK extends Card {

    public MODOK() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, -1, GrantScope.OPPONENT_CREATURES));
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new PayLifeCost(3), new DrawDiscardAndConniveEffect()),
                "Pay 3 life: M.O.D.O.K. connives. Activate only during your turn.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
