package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LRW", collectorNumber = "90")
public class StonybrookAngler extends Card {

    public StonybrookAngler() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(new TapOrUntapTargetPermanentEffect()),
                "{1}{U}, {T}: You may tap or untap target creature.",
                TargetFilters.creature()
        ));
    }
}
