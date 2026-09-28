package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MB2", collectorNumber = "265")
@CardRegistration(set = "MB2", collectorNumber = "501")
public class Indicate extends Card {

    public Indicate() {
        target(TargetFilters.permanent());
    }
}
