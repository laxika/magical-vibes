package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "GTC", collectorNumber = "41")
public class LeylinePhantom extends Card {

    public LeylinePhantom() {
        addEffect(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE, ReturnToHandEffect.self());
    }
}
