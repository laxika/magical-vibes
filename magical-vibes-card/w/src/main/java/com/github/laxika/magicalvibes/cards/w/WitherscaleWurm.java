package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;

@CardRegistration(set = "SHM", collectorNumber = "134")
public class WitherscaleWurm extends Card {

    public WitherscaleWurm() {
        // Whenever this creature blocks or becomes blocked by a creature, that creature gains wither
        // until end of turn. The combat opponent is carried as the trigger's (non-targeting) target.
        addTrigger(Triggers.blocks(), new GrantKeywordEffect(Keyword.WITHER, Subjects.target()));
        addTrigger(Triggers.becomesBlocked(),
                TriggerOptions.EACH_BLOCKER,
                new GrantKeywordEffect(Keyword.WITHER, Subjects.target()));

        // Whenever this creature deals damage to an opponent, remove all -1/-1 counters from it.
        addTrigger(Triggers.dealsDamageToOpponent(),
                RemoveCountersEffect.allFromSelf(CounterType.MINUS_ONE_MINUS_ONE));
    }
}
