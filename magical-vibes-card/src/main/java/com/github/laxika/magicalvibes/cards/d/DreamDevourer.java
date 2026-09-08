package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantForetellToNonlandCardsInHandEffect;

@CardRegistration(set = "KHM", collectorNumber = "90")
public class DreamDevourer extends Card {

    public DreamDevourer() {
        addEffect(EffectSlot.STATIC, new GrantForetellToNonlandCardsInHandEffect());
        addEffect(EffectSlot.ON_CONTROLLER_FORETELLS, new BoostSelfEffect(2, 0));
    }
}
