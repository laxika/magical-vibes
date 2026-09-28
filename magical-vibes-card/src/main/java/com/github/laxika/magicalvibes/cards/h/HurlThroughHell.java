package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndGrantControllerCastPermissionUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AFC", collectorNumber = "48")
public class HurlThroughHell extends Card {

    public HurlThroughHell() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new ExileTargetPermanentAndGrantControllerCastPermissionUntilNextTurnEffect(true));
    }
}
