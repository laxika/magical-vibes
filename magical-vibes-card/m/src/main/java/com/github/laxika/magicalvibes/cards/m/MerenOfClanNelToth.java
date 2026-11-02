package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromGraveyardByExperienceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureFromGraveyardToBattlefieldOrHandByManaValueEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "52")
@CardRegistration(set = "CMM", collectorNumber = "346")
@CardRegistration(set = "CMM", collectorNumber = "584")
@CardRegistration(set = "CMM", collectorNumber = "685")
@CardRegistration(set = "CMA", collectorNumber = "186")
@CardRegistration(set = "C15", collectorNumber = "49")
@CardRegistration(set = "TDC", collectorNumber = "297")
public class MerenOfClanNelToth extends Card {

    public MerenOfClanNelToth() {
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), new ExperienceCountersEffect(1));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ReturnTargetCreatureFromGraveyardToBattlefieldOrHandByManaValueEffect(
                        new ControllerExperienceCounters()));
    }
}
