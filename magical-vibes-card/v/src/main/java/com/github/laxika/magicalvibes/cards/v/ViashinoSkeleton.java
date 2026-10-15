package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "118")
public class ViashinoSkeleton extends Card {

    public ViashinoSkeleton() {
        // {1}{B}, Discard a card: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new DiscardCardTypeCost(null, null), new RegenerateEffect()),
                "{1}{B}, Discard a card: Regenerate this creature."
        ));
    }
}
