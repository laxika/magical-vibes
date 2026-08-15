package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "54")
public class ThassasIre extends Card {

    public ThassasIre() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{U}",
                List.of(new TapOrUntapTargetPermanentEffect()),
                "{3}{U}: You may tap or untap target creature.",
                TargetFilters.creature()
        ));
    }
}
