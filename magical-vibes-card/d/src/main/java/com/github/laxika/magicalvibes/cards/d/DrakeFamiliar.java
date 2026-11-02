package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "44")
public class DrakeFamiliar extends Card {

    public DrakeFamiliar() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SacrificeUnlessReturnPermanentTypeToHandEffect(CardType.ENCHANTMENT));
    }
}
