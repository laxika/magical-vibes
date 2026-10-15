package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PhaseOutChosenTypeNontokenPermanentsEffect;

@CardRegistration(set = "VIS", collectorNumber = "44")
public class TeferisRealm extends Card {

    public TeferisRealm() {
        // EACH_UPKEEP_TRIGGERED puts the active player on targetId so that player chooses the type
        // (not this enchantment's controller). World rule is handled by SBA.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new PhaseOutChosenTypeNontokenPermanentsEffect());
    }
}
