package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "141")
public class TitanForge extends Card {

    public TitanForge() {
        // {3}, {T}: Put a charge counter on Titan Forge.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new PutChargeCounterOnSelfEffect()),
                "{3}, {T}: Put a charge counter on Titan Forge."
        ));

        // {T}, Remove three charge counters from Titan Forge: Create a 9/9 colorless Golem artifact creature token.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveChargeCountersFromSourceCost(3),
                        new CreateCreatureTokenEffect("Golem", 9, 9, null,
                                List.of(CardSubtype.GOLEM), Set.of(), Set.of(CardType.ARTIFACT))
                ),
                "{T}, Remove three charge counters from Titan Forge: Create a 9/9 colorless Golem artifact creature token."
        ));
    }
}
