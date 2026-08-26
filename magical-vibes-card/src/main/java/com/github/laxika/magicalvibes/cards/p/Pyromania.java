package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "112")
public class Pyromania extends Card {

    public Pyromania() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new DiscardRandomCardCost(), new DealDamageToAnyTargetEffect(1)),
                "{1}{R}, Discard a card at random: This enchantment deals 1 damage to any target."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToAnyTargetEffect(1)),
                "{1}{R}, Sacrifice this enchantment: It deals 1 damage to any target."
        ));
    }
}
