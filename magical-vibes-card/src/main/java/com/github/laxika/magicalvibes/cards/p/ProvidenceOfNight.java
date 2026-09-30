package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromMonocoloredEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasHybridManaPredicate;

@CardRegistration(set = "YECL", collectorNumber = "25")
public class ProvidenceOfNight extends Card {

    public ProvidenceOfNight() {
        addEffect(EffectSlot.STATIC, new ProtectionFromMonocoloredEffect());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new CopyControllerCastSpellOnSpellCastEffect(
                        new CardHasHybridManaPredicate(), null, null));
    }
}
