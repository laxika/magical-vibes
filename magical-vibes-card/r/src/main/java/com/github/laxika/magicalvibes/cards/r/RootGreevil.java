package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "91")
public class RootGreevil extends Card {

    public RootGreevil() {
        // {2}{G}, {T}, Sacrifice this creature: Destroy all enchantments of the color of your choice.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(new SacrificeSelfCost(), new DestroyAllPermanentsOfChosenColorEffect(
                        new PermanentIsEnchantmentPredicate())),
                "{2}{G}, {T}, Sacrifice Root Greevil: Destroy all enchantments of the color of your choice."));
    }
}
