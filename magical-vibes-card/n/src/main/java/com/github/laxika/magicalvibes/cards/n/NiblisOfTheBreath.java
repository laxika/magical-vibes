package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "44")
public class NiblisOfTheBreath extends Card {

    public NiblisOfTheBreath() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new MayEffect(new TapOrUntapTargetPermanentEffect(null, true), "Tap or untap target creature?")),
                "{U}, {T}: You may tap or untap target creature.",
                TargetFilters.creature()
        ));
    }
}
