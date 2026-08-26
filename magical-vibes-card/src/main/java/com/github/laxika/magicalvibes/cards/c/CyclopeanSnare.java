package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "259")
public class CyclopeanSnare extends Card {

    public CyclopeanSnare() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        ReturnToHandEffect.self()
                ),
                "{3}, {T}: Tap target creature, then return this artifact to its owner's hand.",
                TargetFilters.creature()
        ));
    }
}
