package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "FDN", collectorNumber = "157")
@CardRegistration(set = "BLB", collectorNumber = "56")
public class LightshellDuo extends Card {

    public LightshellDuo() {
        // Prowess is a Scryfall-loaded keyword, so its boost is handled by the engine.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(2));
    }
}
