package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "832")
public class SpiderManWebSpinner extends Card {

    public SpiderManWebSpinner() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.attacks(), new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
