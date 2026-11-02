package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "109")
public class Petravark extends Card {

    public Petravark() {
        target(TargetFilters.land()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTargetPermanentAndTrackWithSourceEffect());
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new ReturnAllCardsExiledWithSourceEffect());
    }
}
