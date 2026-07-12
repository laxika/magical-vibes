package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromSelfEffect;

@CardRegistration(set = "SHM", collectorNumber = "134")
public class WitherscaleWurm extends Card {

    public WitherscaleWurm() {
        // Whenever this creature blocks or becomes blocked by a creature, that creature gains wither
        // until end of turn. The combat opponent is carried as the trigger's (non-targeting) target.
        addEffect(EffectSlot.ON_BLOCK, new GrantKeywordEffect(Keyword.WITHER, GrantScope.TARGET));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new GrantKeywordEffect(Keyword.WITHER, GrantScope.TARGET),
                TriggerMode.PER_BLOCKER);

        // Whenever this creature deals damage to an opponent, remove all -1/-1 counters from it.
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER,
                new RemoveAllCountersFromSelfEffect(CounterType.MINUS_ONE_MINUS_ONE));
    }
}
