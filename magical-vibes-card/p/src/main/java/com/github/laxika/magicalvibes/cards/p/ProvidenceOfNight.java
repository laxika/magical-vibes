package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromMonocoloredEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasHybridManaPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YECL", collectorNumber = "25")
public class ProvidenceOfNight extends Card {

    public ProvidenceOfNight() {
        addStaticEffect(new ProtectionFromMonocoloredEffect());
        addTrigger(Triggers.youCastSpell(),
                new CopyControllerCastSpellOnSpellCastEffect(
                        new CardHasHybridManaPredicate(), null, null));
    }
}
