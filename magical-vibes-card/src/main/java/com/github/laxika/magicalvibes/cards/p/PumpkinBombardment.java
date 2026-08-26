package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardOrPayManaCost;

@CardRegistration(set = "SPM", collectorNumber = "139")
public class PumpkinBombardment extends Card {

    public PumpkinBombardment() {
        addEffect(EffectSlot.SPELL, new DiscardCardOrPayManaCost("{2}"));
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));
    }
}
