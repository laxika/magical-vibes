package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "88")
public class SoratamiMirrorMage extends Card {

    public SoratamiMirrorMage() {
        // {3}, Return three lands you control to their owner's hand: Return target creature to its
        // owner's hand.
        addActivatedAbility(new ActivatedAbility(false, "{3}",
                List.of(new ReturnMultiplePermanentsToHandCost(3, new PermanentIsLandPredicate()),
                        ReturnToHandEffect.target()),
                "{3}, Return three lands you control to their owner's hand: Return target creature to its owner's hand.",
                TargetFilters.creature()));
    }
}
