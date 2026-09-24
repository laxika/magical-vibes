package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;

@CardRegistration(set = "WWK", collectorNumber = "99")
@CardRegistration(set = "DDO", collectorNumber = "45")
@CardRegistration(set = "MM3", collectorNumber = "125")
@CardRegistration(set = "SLD", collectorNumber = "114")
@CardRegistration(set = "SLD", collectorNumber = "689")
@CardRegistration(set = "TLE", collectorNumber = "259")
public class Explore extends Card {

    public Explore() {
        addEffect(EffectSlot.SPELL, new PlayAdditionalLandsEffect(1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
