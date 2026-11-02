package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.ExileTargetNonlandPermanentAndCardWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryAndReturnExiledCardsEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "153")
@CardRegistration(set = "SPM", collectorNumber = "231")
@CardRegistration(set = "OM1", collectorNumber = "150")
public class TheSpotLivingPortal extends Card {

    public TheSpotLivingPortal() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTargetNonlandPermanentAndCardWithSourceEffect());
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new PutSelfOnBottomOfOwnersLibraryAndReturnExiledCardsEffect());
    }
}
