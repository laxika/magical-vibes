package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.OpponentPoisoned;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "YONE", collectorNumber = "3")
public class NettlingHost extends Card {

    public NettlingHost() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new ConjureCardNamedIntoHandEffect("Nettlecyst", false)
                ),
                "Corrupted — Exile Nettling Host from your graveyard: Conjure a card named Nettlecyst into your hand. "
                        + "Activate only if an opponent has three or more poison counters."
        ).withActivationCondition(
                new OpponentPoisoned(3),
                "An opponent must have at least 3 poison counters"
        ));
    }
}
