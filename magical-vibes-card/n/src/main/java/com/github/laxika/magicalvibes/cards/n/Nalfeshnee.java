package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "186")
public class Nalfeshnee extends Card {

    public Nalfeshnee() {
        addTrigger(Triggers.youCastSpell(),
                CopyControllerCastSpellOnSpellCastEffect.fromExileWithPermanentSpellHasteAndSacrifice());
    }
}
