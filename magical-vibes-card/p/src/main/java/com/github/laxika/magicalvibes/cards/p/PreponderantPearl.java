package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YECL", collectorNumber = "5")
public class PreponderantPearl extends Card {

    public PreponderantPearl() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConjureCardToHandEffect("LRW", "79"));

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)),
                TriggerOptions.ONE_OR_MORE,
                new SacrificeSelfThenEffect(new ConjureCardToHandEffect("ONS", "316")));
    }
}
