package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "95")
public class MistformMutant extends Card {

    public MistformMutant() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect()),
                "{1}{U}: Choose a creature type other than Wall. Target creature becomes that type until end of turn.",
                TargetFilters.creature()
        ));
    }
}
