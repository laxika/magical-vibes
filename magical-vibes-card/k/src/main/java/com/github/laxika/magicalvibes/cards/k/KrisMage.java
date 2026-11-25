package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "195")
public class KrisMage extends Card {

    public KrisMage() {
        addActivatedAbility(new ActivatedAbility(true, "{R}",
                List.of(new DiscardCardTypeCost(null, null), DealDamageEffect.toAnyTarget(1)),
                "{R}, {T}, Discard a card: Kris Mage deals 1 damage to any target."));
    }
}
