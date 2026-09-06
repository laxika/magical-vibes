package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellToHandEffect;

@CardRegistration(set = "SNC", collectorNumber = "209")
public class ObscuraInterceptor extends Card {

    public ObscuraInterceptor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawDiscardAndConniveEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new QueueReflexiveAbilityEffect(
                new ReturnTargetSpellToHandEffect(), true));
    }
}
