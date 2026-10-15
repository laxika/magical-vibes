package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;

@CardRegistration(set = "ALA", collectorNumber = "51")
public class MindlockOrb extends Card {

    public MindlockOrb() {
        // "Players can't search libraries." — absolute, unlike Leonin Arbiter there is no pay-to-ignore.
        addEffect(EffectSlot.STATIC, new CantSearchLibrariesEffect(false));
    }
}
