package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerHasProtectionFromChosenNameEffect;

@CardRegistration(set = "SHM", collectorNumber = "21")
public class RunedHalo extends Card {

    public RunedHalo() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCardNameOnEnterEffect());
        addEffect(EffectSlot.STATIC, new PlayerHasProtectionFromChosenNameEffect());
    }
}
