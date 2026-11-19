package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "5")
public class AuriokTransfixer extends Card {

    public AuriokTransfixer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new TapPermanentsEffect(Subjects.target())),
                "{W}, {T}: Tap target artifact.",
                TargetFilters.artifact()
        ));
    }
}
