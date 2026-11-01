package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnOtherCreatureCardsWithTotalPowerFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIC", collectorNumber = "7")
@CardRegistration(set = "MIC", collectorNumber = "45")
public class MoorlandRescuer extends Card {

    public MoorlandRescuer() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                new ReturnOtherCreatureCardsWithTotalPowerFromGraveyardToBattlefieldEffect(),
                new ExileSourceCardFromGraveyardEffect()));
    }
}
