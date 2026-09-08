package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "7")
public class Hypochondria extends Card {

    public Hypochondria() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new DiscardCardTypeCost(null, null), PreventDamageEffect.nextToTarget(3)),
                "{W}, Discard a card: Prevent the next 3 damage that would be dealt to any target this turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new SacrificeSelfCost(), PreventDamageEffect.nextToTarget(3)),
                "{W}, Sacrifice this enchantment: Prevent the next 3 damage that would be dealt to any target this turn."
        ));
    }
}
