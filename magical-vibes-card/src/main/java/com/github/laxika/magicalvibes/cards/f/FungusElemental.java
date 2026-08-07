package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.SourceEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "128")
public class FungusElemental extends Card {

    public FungusElemental() {
        // {G}, Sacrifice a Forest: Put a +2/+2 counter on this creature.
        // Activate only if this creature entered this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                                "Sacrifice a Forest"
                        ),
                        new PutCountersOnSelfEffect(CounterType.PLUS_TWO_PLUS_TWO)
                ),
                "{G}, Sacrifice a Forest: Put a +2/+2 counter on this creature. "
                        + "Activate only if this creature entered this turn."
        ).withActivationCondition(
                new SourceEnteredBattlefieldThisTurn(),
                "Activate only if this creature entered this turn"));
    }
}
