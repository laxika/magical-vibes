package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromGraveyardByExperienceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureFromGraveyardToBattlefieldOrHandByManaValueEffect;

@CardRegistration(set = "SLD", collectorNumber = "52")
@CardRegistration(set = "C15", collectorNumber = "49")
public class MerenOfClanNelToth extends Card {

    public MerenOfClanNelToth() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new ExperienceCountersEffect(1));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ReturnTargetCreatureFromGraveyardToBattlefieldOrHandByManaValueEffect(
                        new ControllerExperienceCounters()));
    }
}
