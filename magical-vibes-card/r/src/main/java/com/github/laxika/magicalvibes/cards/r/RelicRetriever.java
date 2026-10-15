package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardsLeftGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "SOC", collectorNumber = "35")
@CardRegistration(set = "SOC", collectorNumber = "83")
public class RelicRetriever extends Card {

    public RelicRetriever() {
        // At the beginning of each end step, if a card left your graveyard this turn, create a
        // Treasure token.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new CardsLeftGraveyardThisTurn(), CreateTokenEffect.ofTreasureToken(1)));
    }
}
