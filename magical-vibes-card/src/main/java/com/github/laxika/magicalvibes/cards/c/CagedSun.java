package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddExtraManaOfChosenColorOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;

@CardRegistration(set = "NPH", collectorNumber = "132")
public class CagedSun extends Card {

    public CagedSun() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addEffect(EffectSlot.STATIC, new BoostCreaturesOfChosenColorEffect(1, 1));
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new AddExtraManaOfChosenColorOnLandTapEffect());
    }
}
