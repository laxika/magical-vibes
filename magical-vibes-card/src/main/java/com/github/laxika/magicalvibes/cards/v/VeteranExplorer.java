package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect;

@CardRegistration(set = "WTH", collectorNumber = "144")
@CardRegistration(set = "2XM", collectorNumber = "186")
@CardRegistration(set = "CMD", collectorNumber = "177")
public class VeteranExplorer extends Card {

    public VeteranExplorer() {
        addEffect(EffectSlot.ON_DEATH, new EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect(2));
    }
}
