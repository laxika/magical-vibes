package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ALA", collectorNumber = "32")
@CardRegistration(set = "DDJ", collectorNumber = "18")
public class CallToHeel extends Card {

    public CallToHeel() {
        target(TargetFilters.creature()).addSpellEffect(ReturnToHandEffect.targetAndControllerDraws(1));
    }
}
