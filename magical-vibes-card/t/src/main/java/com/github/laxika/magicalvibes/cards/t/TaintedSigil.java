package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeLostThisTurn;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "83")
public class TaintedSigil extends Card {

    public TaintedSigil() {
        // {T}, Sacrifice Tainted Sigil: You gain life equal to the total life lost by all players this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(),
                        new GainLifeEffect(new LifeLostThisTurn(CountScope.ANY_PLAYER))),
                "{T}, Sacrifice Tainted Sigil: You gain life equal to the total life lost by all players this turn."
        ));
    }
}
