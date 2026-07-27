package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;


@CardRegistration(set = "ARB", collectorNumber = "78")
public class SigilOfTheNayanGods extends Card {

    public SigilOfTheNayanGods() {
        target(TargetFilters.creature())
                // Enchanted creature gets +1/+1 for each creature you control.
                .addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                        new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                        new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                        GrantScope.ENCHANTED_CREATURE));

        // Cycling {G/W} ({G/W}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{G/W}");
    }
}
