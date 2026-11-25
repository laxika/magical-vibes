package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class BloomwielderDryads extends Card {

    public BloomwielderDryads() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), UnlessPaysEffect.counterUnlessPays(2));
        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
