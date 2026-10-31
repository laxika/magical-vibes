package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "XLN", collectorNumber = "161")
@CardRegistration(set = "SLD", collectorNumber = "1862")
@CardRegistration(set = "CMM", collectorNumber = "259")
@CardRegistration(set = "CMM", collectorNumber = "550")
public class StarOfExtinction extends Card {

    public StarOfExtinction() {
        // Destroy target land
        target(TargetFilters.land()).addSpellEffect(new DestroyTargetPermanentEffect());

        // Star of Extinction deals 20 damage to each creature and each planeswalker
        addSpellEffect(new MassDamageEffect(20, false, false, true, null));
    }
}
