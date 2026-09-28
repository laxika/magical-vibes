package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMaySearchLibraryForBasicLandsToMatchMostLandsEffect;

@CardRegistration(set = "C21", collectorNumber = "22")
public class ScholarshipSponsor extends Card {

    public ScholarshipSponsor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachPlayerMaySearchLibraryForBasicLandsToMatchMostLandsEffect());
    }
}
