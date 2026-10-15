package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

@CardRegistration(set = "ROE", collectorNumber = "187")
public class HazeFrog extends Card {

    public HazeFrog() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, PreventDamageEffect.allCombatExcept(
                new PermanentIsSourcePermanentPredicate()));
    }
}
