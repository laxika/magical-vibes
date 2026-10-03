package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;

@CardRegistration(set = "SLD", collectorNumber = "1495")
@CardRegistration(set = "SLD", collectorNumber = "1665")
@CardRegistration(set = "C15", collectorNumber = "55")
@CardRegistration(set = "SLZ", collectorNumber = "116")
@CardRegistration(set = "SLZ", collectorNumber = "237")
@CardRegistration(set = "SLZ", collectorNumber = "358")
@CardRegistration(set = "MSC", collectorNumber = "222")
@CardRegistration(set = "CMM", collectorNumber = "414")
@CardRegistration(set = "WHO", collectorNumber = "255")
@CardRegistration(set = "PIP", collectorNumber = "251")
@CardRegistration(set = "PIP", collectorNumber = "779")
@CardRegistration(set = "MB2", collectorNumber = "100")
@CardRegistration(set = "40K", collectorNumber = "259")
@CardRegistration(set = "DSC", collectorNumber = "256")
@CardRegistration(set = "LTC", collectorNumber = "287")
@CardRegistration(set = "MKC", collectorNumber = "245")
@CardRegistration(set = "LCC", collectorNumber = "118")
@CardRegistration(set = "BLC", collectorNumber = "289")
@CardRegistration(set = "WHO", collectorNumber = "846")
@CardRegistration(set = "BRC", collectorNumber = "167")
public class ThoughtVessel extends Card {

    public ThoughtVessel() {
        // You have no maximum hand size.
        addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());

        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
