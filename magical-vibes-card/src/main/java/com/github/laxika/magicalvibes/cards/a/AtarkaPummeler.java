package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlledCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "129")
public class AtarkaPummeler extends Card {

    public AtarkaPummeler() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}{R}",
                List.of(new GrantKeywordEffect(Keyword.MENACE, GrantScope.ALL_OWN_CREATURES)),
                "{3}{R}{R}: Creatures you control gain menace until end of turn. Activate only if creatures you control have total power 8 or greater."
        ).withActivationCondition(
                new ControlledCreaturesTotalPowerAtLeast(8),
                "Activate only if creatures you control have total power 8 or greater."
        ));
    }
}
