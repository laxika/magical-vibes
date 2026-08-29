package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.InvestigateForEachTargetPlayerCreatureEffect;

@CardRegistration(set = "MKM", collectorNumber = "222")
@CardRegistration(set = "MKM", collectorNumber = "314")
public class OfficiousInterrogation extends Card {

    public OfficiousInterrogation() {
        setAdditionalManaCostPerExtraTarget("{W}{U}");
        target(0, 99).addEffect(EffectSlot.SPELL, new InvestigateForEachTargetPlayerCreatureEffect());
    }
}
