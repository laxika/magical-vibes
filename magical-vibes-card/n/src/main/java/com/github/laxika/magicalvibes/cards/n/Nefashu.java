package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "70")
@CardRegistration(set = "HOP", collectorNumber = "34")
public class Nefashu extends Card {

    public Nefashu() {
        target(TargetFilters.creature(), 0, 5)
                .addTrigger(Triggers.attacks(), new BoostTargetCreatureEffect(-1, -1));
    }
}
