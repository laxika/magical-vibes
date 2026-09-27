package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjurePowerNineIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "YDMU", collectorNumber = "4")
public class OracleOfTheAlpha extends Card {

    public OracleOfTheAlpha() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConjurePowerNineIntoLibraryEffect());
        addEffect(EffectSlot.ON_ATTACK, new ScryEffect(1));
    }
}
