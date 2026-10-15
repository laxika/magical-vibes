package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEM", collectorNumber = "101")
public class AnimateLand extends Card {

    public AnimateLand() {
        // Target land becomes a 3/3 creature until end of turn. It's still a land.
        target(TargetFilters.land()).addEffect(EffectSlot.SPELL, new AnimatePermanentsEffect(
                3, 3,
                List.of(), Set.of(),
                null, Set.of(),
                GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN));
    }
}
