package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MIC", collectorNumber = "7")
@CardRegistration(set = "MIC", collectorNumber = "45")
public class MoorlandRescuer extends Card {

    public MoorlandRescuer() {
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect(),
                new ExileSourceCardFromGraveyardEffect()));
    }
}
