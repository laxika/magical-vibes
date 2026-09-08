package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VOW", collectorNumber = "24")
public class MilitiaRallier extends Card {

    public MilitiaRallier() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockAloneEffect(false));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ATTACK,
                        new UntapPermanentsEffect(TapUntapScope.TARGET, new PermanentIsCreaturePredicate()));
    }
}
