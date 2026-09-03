package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "45")
public class NovaCleric extends Card {

    public NovaCleric() {
        // {2}{W}, {T}, Sacrifice this creature: Destroy all enchantments.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}",
                List.of(new SacrificeSelfCost(), new DestroyAllPermanentsEffect(
                        new PermanentIsEnchantmentPredicate())),
                "{2}{W}, {T}, Sacrifice Nova Cleric: Destroy all enchantments."
        ));
    }
}
