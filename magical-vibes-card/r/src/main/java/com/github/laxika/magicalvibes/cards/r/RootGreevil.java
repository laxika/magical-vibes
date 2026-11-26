package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ClearChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasChosenSpellColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "91")
public class RootGreevil extends Card {

    public RootGreevil() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(new SacrificeSelfCost(),
                        new ChooseColorAtResolutionEffect(),
                        DestroyTargetPermanentEffect.all(new PermanentAllOfPredicate(List.of(
                                new PermanentIsEnchantmentPredicate(), new PermanentHasChosenSpellColorPredicate()))),
                        new ClearChosenColorEffect()),
                "{2}{G}, {T}, Sacrifice Root Greevil: Destroy all enchantments of the color of your choice."));
    }
}
