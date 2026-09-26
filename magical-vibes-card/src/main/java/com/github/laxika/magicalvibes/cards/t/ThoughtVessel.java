package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;

@CardRegistration(set = "SLD", collectorNumber = "1495")
@CardRegistration(set = "SLD", collectorNumber = "1665")
@CardRegistration(set = "SLD", collectorNumber = "2122")
@CardRegistration(set = "C15", collectorNumber = "55")
@CardRegistration(set = "SLZ", collectorNumber = "116")
@CardRegistration(set = "SLZ", collectorNumber = "237")
@CardRegistration(set = "SLZ", collectorNumber = "358")
@CardRegistration(set = "CMM", collectorNumber = "414")
@CardRegistration(set = "LTC", collectorNumber = "287")
public class ThoughtVessel extends Card {

    public ThoughtVessel() {
        // You have no maximum hand size.
        addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());

        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
