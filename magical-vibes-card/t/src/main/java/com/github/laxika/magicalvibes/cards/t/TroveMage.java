package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.SeekFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDMU", collectorNumber = "6")
public class TroveMage extends Card {

    public TroveMage() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SeekFromTopOfLibraryEffect(10, new CardTypePredicate(CardType.ARTIFACT)));
    }
}
