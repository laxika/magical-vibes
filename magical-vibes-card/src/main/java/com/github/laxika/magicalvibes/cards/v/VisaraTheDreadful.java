package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "179")
public class VisaraTheDreadful extends Card {

    public VisaraTheDreadful() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DestroyTargetPermanentEffect(true)),
                "{T}: Destroy target creature. It can't be regenerated.",
                TargetFilters.creature()
        ));
    }
}
