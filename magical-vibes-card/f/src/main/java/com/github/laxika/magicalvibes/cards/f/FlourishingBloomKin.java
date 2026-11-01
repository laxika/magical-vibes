package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "160")
public class FlourishingBloomKin extends Card {

    public FlourishingBloomKin() {
        PermanentCount forestsYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.CONTROLLER);
        addStaticEffect(new BoostSelfEffect(forestsYouControl, forestsYouControl));

        addMorph("{4}{G}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect.landSubtype(CardSubtype.FOREST));
    }
}
