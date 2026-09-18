package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "5")
public class Haystack extends Card {

    public Haystack() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new PhaseOutEffect(PhaseOutSubject.TARGET)),
                "{2}, {T}: Target creature you control phases out.",
                TargetFilters.creatureYouControl()
        ));
    }
}
