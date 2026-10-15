package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "119")
public class CacklingWitch extends Card {

    public CacklingWitch() {
        // {X}{B}, {T}, Discard a card: Target creature gets +X/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{B}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new BoostTargetCreatureEffect(new XValue(), new Fixed(0))
                ),
                "{X}{B}, {T}, Discard a card: Target creature gets +X/+0 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
