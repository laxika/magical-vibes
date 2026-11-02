package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "158")
@CardRegistration(set = "OM1", collectorNumber = "152")
public class VultureSchemingScavenger extends Card {

    public VultureSchemingScavenger() {
        addTrigger(Triggers.attacks(),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.VILLAIN)));
    }
}
