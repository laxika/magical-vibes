package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachCreatureAndPlaneswalkerDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "132")
@CardRegistration(set = "MKM", collectorNumber = "349")
@CardRegistration(set = "MKM", collectorNumber = "381")
public class IncineratorOfTheGuilty extends Card {

    public IncineratorOfTheGuilty() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(
                SequenceEffect.of(
                        CollectEvidenceEffect.withChosenX(),
                        new QueueReflexiveAbilityEffect(
                                new DealDamageToEachCreatureAndPlaneswalkerDamagedPlayerControlsEffect(),
                                false, true)),
                "Collect evidence X?"));
    }
}
