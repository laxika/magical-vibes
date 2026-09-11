package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ZNR", collectorNumber = "1")
public class AlliedAssault extends Card {

    public AlliedAssault() {
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(new PartySize(), new PartySize()));
    }
}
