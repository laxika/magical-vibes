package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "246")
public class BrionStoutarm extends Card {

    public BrionStoutarm() {
        // Lifelink is auto-loaded from Scryfall.

        // {R}, {T}, Sacrifice another creature: Brion Stoutarm deals damage equal to the
        // sacrificed creature's power to target player or planeswalker.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(
                        new SacrificeCreatureCost(false, true, false, true),
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(new XValue())
                ),
                "{R}, {T}, Sacrifice another creature: Brion Stoutarm deals damage equal to the "
                        + "sacrificed creature's power to target player or planeswalker."
        ));
    }
}
