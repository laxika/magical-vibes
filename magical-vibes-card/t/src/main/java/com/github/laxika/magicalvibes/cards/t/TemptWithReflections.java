package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "C13", collectorNumber = "60")
public class TemptWithReflections extends Card {

    public TemptWithReflections() {
        CreateTokenCopyOfTargetPermanentEffect copy = new CreateTokenCopyOfTargetPermanentEffect();
        target(TargetFilters.creatureYouControl()).addSpellEffect(copy);
        addSpellEffect(TemptingOfferEffect.of(
                "Create a token that's a copy of that creature? If you do, its controller creates another one.",
                copy::forController));
    }
}
