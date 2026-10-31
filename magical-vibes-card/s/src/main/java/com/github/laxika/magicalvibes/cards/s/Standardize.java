package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect;

@CardRegistration(set = "ONS", collectorNumber = "116")
public class Standardize extends Card {

    public Standardize() {
        addSpellEffect(new TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(GrantScope.ALL_CREATURES));
    }
}
