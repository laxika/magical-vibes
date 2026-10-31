package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardOrPayManaCost;

@CardRegistration(set = "SPM", collectorNumber = "139")
@CardRegistration(set = "OM1", collectorNumber = "142")
@CardRegistration(set = "MSC", collectorNumber = "822")
public class PumpkinBombardment extends Card {

    public PumpkinBombardment() {
        addCastingCost(new DiscardCardOrPayManaCost("{2}"));
        addSpellEffect(new DealDamageToTargetCreatureEffect(3));
    }
}
