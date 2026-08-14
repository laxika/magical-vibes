package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "M12", collectorNumber = "106")
@CardRegistration(set = "FDN", collectorNumber = "184")
public class RuneScarredDemon extends Card {

    public RuneScarredDemon() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SearchLibraryEffect());
    }
}
