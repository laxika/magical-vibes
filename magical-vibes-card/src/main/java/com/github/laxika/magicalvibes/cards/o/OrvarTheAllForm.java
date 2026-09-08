package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetedSpellPermanentEffect;

@CardRegistration(set = "KHM", collectorNumber = "70")
public class OrvarTheAllForm extends Card {

    public OrvarTheAllForm() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new CreateTokenCopyOfTargetedSpellPermanentEffect());
        addEffect(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT,
                new CreateTokenCopyOfTargetPermanentEffect());
    }
}
