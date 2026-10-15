package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerEffect;

@CardRegistration(set = "M12", collectorNumber = "30")
public class PersonalSanctuary extends Card {

    public PersonalSanctuary() {
        // During your turn, prevent all damage that would be dealt to you.
        addEffect(EffectSlot.STATIC, new PreventAllDamageToControllerEffect(true));
    }
}
