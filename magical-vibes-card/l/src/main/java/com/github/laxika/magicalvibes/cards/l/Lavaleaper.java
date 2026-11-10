package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "150")
@CardRegistration(set = "ECL", collectorNumber = "318")
public class Lavaleaper extends Card {

    public Lavaleaper() {
        addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_CREATURES_INCLUDING_SELF));
        // Whenever a player taps a basic land for mana, that player adds one mana of any type that land produced.
        addTrigger(Triggers.playerTapsLandForMana().withFilter(new PermanentHasSupertypePredicate(CardSupertype.BASIC)),
                new AddOneOfEachManaTypeProducedByLandEffect());
    }
}
