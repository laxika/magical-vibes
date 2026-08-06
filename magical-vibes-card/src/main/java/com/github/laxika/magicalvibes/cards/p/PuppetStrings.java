package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "304")
public class PuppetStrings extends Card {

    public PuppetStrings() {
        // {2}, {T}: You may tap or untap target creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new TapOrUntapTargetPermanentEffect()),
                "{2}, {T}: You may tap or untap target creature.",
                TargetFilters.creature()
        ));
    }
}
