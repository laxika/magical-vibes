package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastForetoldCardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantForetellToNonlandCardsInHandEffect;

@CardRegistration(set = "WHO", collectorNumber = "598")
public class SingingTowersOfDarillium extends Card {

    public SingingTowersOfDarillium() {
        addEffect(EffectSlot.STATIC, new GrantForetellToNonlandCardsInHandEffect());
        addEffect(EffectSlot.CHAOS_TRIGGERED, new AllowCastForetoldCardThisTurnEffect());
    }
}
