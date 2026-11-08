package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDMU", collectorNumber = "14")
public class GoblinMoraleSergeant extends Card {

    public GoblinMoraleSergeant() {
        addTrigger(Triggers.enlists().withFilter(new PermanentNotPredicate(new PermanentIsTokenPredicate())),
                new MayEffect(new ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect(),
                        "Conjure a duplicate of the enlisted creature into the top five cards of your library?"));
    }
}
