package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect;

@CardRegistration(set = "CHK", collectorNumber = "225")
@CardRegistration(set = "MMA", collectorNumber = "151")
@CardRegistration(set = "SLD", collectorNumber = "371")
@CardRegistration(set = "SLD", collectorNumber = "2294")
@CardRegistration(set = "UMA", collectorNumber = "171")
@CardRegistration(set = "ECC", collectorNumber = "113")
@CardRegistration(set = "CMD", collectorNumber = "163")
@CardRegistration(set = "C15", collectorNumber = "188")
@CardRegistration(set = "CMM", collectorNumber = "300")
@CardRegistration(set = "CMM", collectorNumber = "649")
public class KodamasReach extends Card {

    public KodamasReach() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect());
    }
}
