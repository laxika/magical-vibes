package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "87")
public class Imagecrafter extends Card {

    public Imagecrafter() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect()),
                "{T}: Choose a creature type other than Wall. Target creature becomes that type until end of turn.",
                TargetFilters.creature()
        ));
    }
}
