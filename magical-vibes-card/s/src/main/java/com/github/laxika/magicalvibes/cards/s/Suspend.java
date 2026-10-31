package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureWithSuspendEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MH2", collectorNumber = "68")
public class Suspend extends Card {

    public Suspend() {
        target(TargetFilters.creature())
                .addSpellEffect(new ExileTargetCreatureWithSuspendEffect(2));
    }
}
