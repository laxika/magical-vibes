package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCreatureToBattlefieldOrHandByManaValueEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "134")
@CardRegistration(set = "HOC", collectorNumber = "134")
public class PartInFriendship extends Card {

    public PartInFriendship() {
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.YOU_CONTROL),
                new OncePerTurnTriggerEffect(
                        new RevealUntilCreatureToBattlefieldOrHandByManaValueEffect()));
    }
}
