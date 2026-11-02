package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YMKM", collectorNumber = "5")
public class EmporiumThopterist extends Card {

    public EmporiumThopterist() {
        addStaticEffect(new StaticBoostEffect(2, 0, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.THOPTER)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ConjureCardNamedIntoHandEffect("ATQ", "60", false));
    }
}
