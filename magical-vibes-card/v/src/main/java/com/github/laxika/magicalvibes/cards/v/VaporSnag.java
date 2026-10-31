package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NPH", collectorNumber = "48")
@CardRegistration(set = "DDH", collectorNumber = "58")
@CardRegistration(set = "MM2", collectorNumber = "66")
public class VaporSnag extends Card {

    public VaporSnag() {
        target(TargetFilters.creature()).addSpellEffect(ReturnToHandEffect.targetAndControllerLosesLife(1));
    }
}
