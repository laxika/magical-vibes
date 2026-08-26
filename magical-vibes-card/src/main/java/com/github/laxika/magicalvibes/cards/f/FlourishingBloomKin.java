package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MKM", collectorNumber = "160")
public class FlourishingBloomKin extends Card {

    public FlourishingBloomKin() {
        PermanentCount forestsYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(forestsYouControl, forestsYouControl));

        addMorph("{4}{G}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect.landSubtype(CardSubtype.FOREST));
    }
}
