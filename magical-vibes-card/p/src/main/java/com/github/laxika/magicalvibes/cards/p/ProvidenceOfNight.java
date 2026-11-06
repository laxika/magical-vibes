package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromMonocoloredEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasHybridManaPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;

@CardRegistration(set = "YECL", collectorNumber = "25")
public class ProvidenceOfNight extends Card {

    public ProvidenceOfNight() {
        addStaticEffect(new ProtectionFromMonocoloredEffect());
        addTrigger(Triggers.youCastSpell().withFilter(new CardHasHybridManaPredicate()),
                new CopyTriggeringSpellEffect());
    }
}
