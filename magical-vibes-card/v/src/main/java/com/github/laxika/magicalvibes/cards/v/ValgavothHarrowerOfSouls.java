package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSC", collectorNumber = "6")
public class ValgavothHarrowerOfSouls extends Card {

    public ValgavothHarrowerOfSouls() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                UnlessPaysEffect.counterUnlessPaysLife(2));
        addTrigger(Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                new DrawCardEffect(1));
    }
}
