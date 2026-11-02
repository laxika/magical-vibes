package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileArtifactOrCreatureCardFromHandOrGraveyardWithCageCounterEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCardsExiledWithCageCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C17", collectorNumber = "41")
public class MairsilThePretender extends Card {

    public MairsilThePretender() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileArtifactOrCreatureCardFromHandOrGraveyardWithCageCounterEffect());
        addStaticEffect(new GainActivatedAbilitiesOfCardsExiledWithCageCountersEffect());
    }
}
