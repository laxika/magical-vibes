package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MID", collectorNumber = "56")
@CardRegistration(set = "DBL", collectorNumber = "56")
public class Geistwave extends Card {

    public Geistwave() {
        // Return target nonland permanent to its owner's hand. If you controlled that permanent,
        // draw a card. Control is checked before the target leaves the battlefield.
        target(TargetFilters.nonlandPermanent())
                .addSpellEffect(ReturnToHandEffect.target())
                        .addSpellEffect(new ConditionalEffect(new TargetPermanentMatches(new PermanentControlledBySourceControllerPredicate()), new DrawCardEffect(1)));
    }
}
