package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileArtifactOrCreatureCardFromHandOrGraveyardWithCageCounterEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCardsExiledWithCageCountersEffect;

@CardRegistration(set = "C17", collectorNumber = "41")
public class MairsilThePretender extends Card {

    public MairsilThePretender() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileArtifactOrCreatureCardFromHandOrGraveyardWithCageCounterEffect());
        addEffect(EffectSlot.STATIC, new GainActivatedAbilitiesOfCardsExiledWithCageCountersEffect());
    }
}
