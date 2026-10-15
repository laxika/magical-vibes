package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;

@CardRegistration(set = "HBG", collectorNumber = "186")
public class Nalfeshnee extends Card {

    public Nalfeshnee() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                CopyControllerCastSpellOnSpellCastEffect.fromExileWithPermanentSpellHasteAndSacrifice());
    }
}
