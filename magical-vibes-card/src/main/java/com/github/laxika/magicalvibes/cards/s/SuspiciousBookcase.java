package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "246")
public class SuspiciousBookcase extends Card {

    public SuspiciousBookcase() {
        addActivatedAbility(new ActivatedAbility(true, "{3}", List.of(new MakeCreatureUnblockableEffect()),
                "{3}, {T}: Target creature can't be blocked this turn.", TargetFilters.creature()));
    }
}
