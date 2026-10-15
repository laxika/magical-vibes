package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "152")
public class GilderBairn extends Card {

    public GilderBairn() {
        // "{2}{G/U}, {Q}: Double the number of each kind of counter on target permanent."
        addActivatedAbility(new ActivatedAbility(
                false,  // {Q} untaps rather than taps
                "{2}{G/U}",
                List.of(new DoubleCountersOnTargetPermanentEffect()),
                "{2}{G/U}, {Q}: Double the number of each kind of counter on target permanent.",
                TargetFilters.permanent()
        ).withRequiresUntap());
    }
}
