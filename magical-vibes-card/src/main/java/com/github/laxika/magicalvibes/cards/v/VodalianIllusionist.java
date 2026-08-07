package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "58")
public class VodalianIllusionist extends Card {

    public VodalianIllusionist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}{U}",
                List.of(new PhaseOutEffect(PhaseOutSubject.TARGET)),
                "{U}{U}, {T}: Target creature phases out.",
                TargetFilters.creature()
        ));
    }
}
