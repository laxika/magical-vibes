package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMaySearchLibraryForCardsToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "44")
public class NobleBenefactor extends Card {

    public NobleBenefactor() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), EachPlayerMaySearchLibraryForCardsToHandEffect.oneCard());
    }
}
