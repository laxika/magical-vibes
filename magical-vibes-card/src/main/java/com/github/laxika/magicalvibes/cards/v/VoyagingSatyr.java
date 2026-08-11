package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "182")
public class VoyagingSatyr extends Card {

    public VoyagingSatyr() {
        // {T}: Untap target land.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET, new PermanentIsLandPredicate())),
                "{T}: Untap target land.",
                TargetFilters.land()));
    }
}
