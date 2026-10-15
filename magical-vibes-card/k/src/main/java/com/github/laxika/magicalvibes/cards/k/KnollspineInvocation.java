package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "99")
public class KnollspineInvocation extends Card {

    public KnollspineInvocation() {
        // {X}, Discard a card with mana value X: deal X damage to any target.
        // The discard cost is constrained to a card whose mana value equals the chosen X.
        addActivatedAbility(new ActivatedAbility(false, "{X}",
                List.of(new DiscardCardTypeCost(null, null, true), new DealDamageToAnyTargetEffect(new XValue())),
                "{X}, Discard a card with mana value X: Knollspine Invocation deals X damage to any target."));
    }
}
