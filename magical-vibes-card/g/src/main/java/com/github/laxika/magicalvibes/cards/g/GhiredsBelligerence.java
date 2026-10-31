package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedDamagedCreatureDeathTriggerEffect;

@CardRegistration(set = "C19", collectorNumber = "25")
public class GhiredsBelligerence extends Card {

    public GhiredsBelligerence() {
        addSpellEffect(DealDividedDamageEffect.xAmongTargetCreatures());
        addSpellEffect(new RegisterDelayedDamagedCreatureDeathTriggerEffect(
                new PopulateEffect(), true));
    }
}
