package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SCG", collectorNumber = "15")
public class FrontlineStrategist extends Card {

    public FrontlineStrategist() {
        addMorph("{W}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                PreventDamageEffect.allCombatExcept(new PermanentHasSubtypePredicate(CardSubtype.SOLDIER)));
    }
}
