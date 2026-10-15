package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

public class WerewolfOfAncientHunger extends Card {

    public WerewolfOfAncientHunger() {
        CardsInHand cardsInAllPlayersHands = new CardsInHand(CountScope.ANY_PLAYER);
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(cardsInAllPlayersHands, cardsInAllPlayersHands));
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new TransformSelfEffect()));
    }
}
