package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "98")
public class TradewindRider extends Card {

    public TradewindRider() {
        // {T}, Tap two untapped creatures you control: Return target permanent to its owner's hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "",
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentIsCreaturePredicate(), true),
                        ReturnToHandEffect.target()),
                "{T}, Tap two untapped creatures you control: Return target permanent to its owner's hand.",
                TargetFilters.permanent()));
    }
}
