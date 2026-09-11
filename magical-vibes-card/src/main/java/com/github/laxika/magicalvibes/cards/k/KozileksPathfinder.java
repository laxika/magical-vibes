package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "5")
public class KozileksPathfinder extends Card {

    public KozileksPathfinder() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{C}",
                List.of(new CantBlockSourceEffect(null)),
                "{C}: Target creature can't block Kozilek's Pathfinder this turn.",
                TargetFilters.creature()
        ));
    }
}
