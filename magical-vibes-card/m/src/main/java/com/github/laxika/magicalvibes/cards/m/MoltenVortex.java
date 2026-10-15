package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "156")
public class MoltenVortex extends Card {

    public MoltenVortex() {
        // {R}, Discard a land card: This enchantment deals 2 damage to any target.
        addActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(new DiscardCardTypeCost(new CardTypePredicate(CardType.LAND), "land"),
                        new DealDamageToAnyTargetEffect(2)),
                "{R}, Discard a land card: Molten Vortex deals 2 damage to any target."));
    }
}
