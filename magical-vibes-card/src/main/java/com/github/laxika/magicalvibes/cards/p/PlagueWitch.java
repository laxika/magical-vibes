package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "66")
public class PlagueWitch extends Card {

    public PlagueWitch() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new BoostTargetCreatureEffect(-1, -1)
                ),
                "{B}, {T}, Discard a card: Target creature gets -1/-1 until end of turn."
        ));
    }
}
