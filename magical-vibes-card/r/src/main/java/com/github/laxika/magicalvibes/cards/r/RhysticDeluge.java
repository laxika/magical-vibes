package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "43")
public class RhysticDeluge extends Card {

    public RhysticDeluge() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new MayPayManaEffect(
                        "{1}",
                        null,
                        "Pay {1} to keep the target creature untapped?",
                        MayPayPayer.TARGET_PERMANENT_CONTROLLER,
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        0
                )),
                "{U}: Tap target creature unless its controller pays {1}.",
                TargetFilters.creature()
        ));
    }
}
