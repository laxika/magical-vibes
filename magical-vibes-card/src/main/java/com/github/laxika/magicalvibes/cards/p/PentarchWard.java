package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenColorEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TSP", collectorNumber = "33")
public class PentarchWard extends Card {

    public PentarchWard() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1))
                .addEffect(EffectSlot.STATIC,
                        new ProtectionFromChosenColorEffect(GrantScope.ENCHANTED_CREATURE));
    }
}
