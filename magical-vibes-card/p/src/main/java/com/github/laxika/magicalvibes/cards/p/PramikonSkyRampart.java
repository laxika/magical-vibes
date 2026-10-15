package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttackOnlyNearestOpponentInDirectionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;

import java.util.List;

@CardRegistration(set = "C19", collectorNumber = "47")
public class PramikonSkyRampart extends Card {

    public PramikonSkyRampart() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseModeOnEnterEffect(List.of("Left", "Right")));
        addEffect(EffectSlot.STATIC, new AttackOnlyNearestOpponentInDirectionEffect());
    }
}
