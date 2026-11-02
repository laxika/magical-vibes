package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "185")
@CardRegistration(set = "LCI", collectorNumber = "326")
public class GhaltaStampedeTyrant extends Card {

    public GhaltaStampedeTyrant() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                PutCardToBattlefieldEffect.anyNumber(new CardTypePredicate(CardType.CREATURE), "creature"));
    }
}
