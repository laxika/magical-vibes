package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerEqualsToughnessPredicate;

@CardRegistration(set = "BRO", collectorNumber = "252")
public class SymmetryMatrix extends Card {

    public SymmetryMatrix() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentPowerEqualsToughnessPredicate(),
                        new MayPayManaEffect("{1}", new DrawCardEffect(1), "Pay {1} to draw a card?")));
    }
}
