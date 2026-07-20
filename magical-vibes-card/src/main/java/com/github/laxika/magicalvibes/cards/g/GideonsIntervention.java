package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenNameEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsWithChosenNameCantBeCastEffect;

@CardRegistration(set = "AKH", collectorNumber = "15")
public class GideonsIntervention extends Card {

    public GideonsIntervention() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCardNameOnEnterEffect());
        // Your opponents can't cast spells with the chosen name (opponents-only).
        addEffect(EffectSlot.STATIC, new SpellsWithChosenNameCantBeCastEffect(true));
        // Prevent all damage to you and permanents you control by sources with the chosen name.
        addEffect(EffectSlot.STATIC, new PreventDamageFromChosenNameEffect());
    }
}
