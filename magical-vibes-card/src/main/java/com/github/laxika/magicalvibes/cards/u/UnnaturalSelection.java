package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "32")
public class UnnaturalSelection extends Card {

    public UnnaturalSelection() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect()),
                "{1}: Choose a creature type other than Wall. Target creature becomes that type until end of turn.",
                TargetFilters.creature()
        ));
    }
}
