package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenPlayerModeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;

import java.util.List;

@CardRegistration(set = "V15", collectorNumber = "3")
public class ArchangelOfStrife extends Card {

    private static final String WAR = "War";
    private static final String PEACE = "Peace";

    public ArchangelOfStrife() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseModeOnEnterEffect(List.of(WAR, PEACE), true));
        addEffect(EffectSlot.STATIC, new BoostCreaturesOfChosenPlayerModeEffect(WAR, 3, 0));
        addEffect(EffectSlot.STATIC, new BoostCreaturesOfChosenPlayerModeEffect(PEACE, 0, 3));
    }
}
