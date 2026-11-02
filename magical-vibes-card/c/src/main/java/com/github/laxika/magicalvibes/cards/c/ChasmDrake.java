package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M12", collectorNumber = "48")
public class ChasmDrake extends Card {

    public ChasmDrake() {
        // Flying is loaded from Scryfall.
        // Whenever this creature attacks, target creature you control gains flying until end of turn.
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.attacks(), new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));
    }
}
