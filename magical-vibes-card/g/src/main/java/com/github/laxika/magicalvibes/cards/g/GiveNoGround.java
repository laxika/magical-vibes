package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EMN", collectorNumber = "29")
public class GiveNoGround extends Card {

    public GiveNoGround() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 6))
                .addSpellEffect(new CanBlockAnyNumberOfCreaturesUntilEndOfTurnEffect());
    }
}
