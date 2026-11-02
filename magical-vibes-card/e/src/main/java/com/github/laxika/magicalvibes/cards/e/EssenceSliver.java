package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SourceDamageGainLifeForSourceControllerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSB", collectorNumber = "8")
@CardRegistration(set = "LGN", collectorNumber = "13")
@CardRegistration(set = "SLD", collectorNumber = "612")
public class EssenceSliver extends Card {

    public EssenceSliver() {
        // Whenever a Sliver deals damage, its controller gains that much life.
        addTrigger(Triggers.sourceDealsDamage(),
                new SourceDamageGainLifeForSourceControllerEffect(new CardSubtypePredicate(CardSubtype.SLIVER)));
    }
}
