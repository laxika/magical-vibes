package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DIS", collectorNumber = "20")
public class ValorMadeReal extends Card {

    public ValorMadeReal() {
        target(TargetFilters.creature())
                .addSpellEffect(new CanBlockAnyNumberOfCreaturesUntilEndOfTurnEffect());
    }
}
