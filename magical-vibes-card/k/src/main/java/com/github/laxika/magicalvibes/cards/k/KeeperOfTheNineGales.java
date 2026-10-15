package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "42")
public class KeeperOfTheNineGales extends Card {

    public KeeperOfTheNineGales() {
        // {T}, Tap two untapped Birds you control: Return target permanent to its owner's hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "",
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.BIRD), true),
                        ReturnToHandEffect.target()),
                "{T}, Tap two untapped Birds you control: Return target permanent to its owner's hand.",
                TargetFilters.permanent()));
    }
}
