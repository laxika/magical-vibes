package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByMonarchPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCC", collectorNumber = "144")
public class AzureFleetAdmiral extends Card {

    public AzureFleetAdmiral() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BecomeMonarchEffect());
        // "This creature can't be blocked by creatures the monarch controls."
        addStaticEffect(new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentControlledByMonarchPredicate()));
    }
}
