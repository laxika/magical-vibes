package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "115")
public class Blockbuster extends Card {

    public Blockbuster() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(
                        new SacrificeSelfCost(),
                        new MassDamageEffect(3, false, true, new PermanentIsTappedPredicate())
                ),
                "{1}{R}, Sacrifice this enchantment: It deals 3 damage to each tapped creature and each player."
        ));
    }
}
