package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromGraveyardByExperienceEffect;

@CardRegistration(set = "SLD", collectorNumber = "52")
public class MerenOfClanNelToth extends Card {

    public MerenOfClanNelToth() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new ExperienceCountersEffect(1));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ReturnTargetCardFromGraveyardByExperienceEffect());
    }
}
