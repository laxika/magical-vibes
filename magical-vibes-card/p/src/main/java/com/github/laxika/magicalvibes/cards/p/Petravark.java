package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "109")
public class Petravark extends Card {

    public Petravark() {
        target(TargetFilters.land()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetPermanentAndTrackWithSourceEffect());
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new ReturnAllCardsExiledWithSourceEffect());
    }
}
