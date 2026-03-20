package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEffect;

@CardRegistration(set = "DOM", collectorNumber = "129")
public class GoblinChainwhirler extends Card {

    public GoblinChainwhirler() {
        // When Goblin Chainwhirler enters the battlefield, it deals 1 damage to each opponent
        // and each creature and planeswalker they control.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToEachOpponentEffect(1));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffect(1));
    }
}
