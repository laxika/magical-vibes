package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "46")
public class BoomerangBasics extends Card {

    public BoomerangBasics() {
        target(TargetFilters.nonlandPermanent())
                .addSpellEffect(ReturnToHandEffect.target())
                        .addSpellEffect(new ConditionalEffect(new TargetPermanentMatches(new PermanentControlledBySourceControllerPredicate()), new DrawCardEffect(1)));
    }
}
