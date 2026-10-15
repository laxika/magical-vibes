package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.APlayerHasMoreCardsInHandThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerWithMostCardsInHandGainsControlOfSourceCreatureEffect;

@CardRegistration(set = "SOK", collectorNumber = "114")
public class SokenzanRenegade extends Card {

    public SokenzanRenegade() {
        addEffect(EffectSlot.ON_BLOCK, new BushidoEffect(1));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BushidoEffect(1));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new APlayerHasMoreCardsInHandThanEachOtherPlayer(),
                new PlayerWithMostCardsInHandGainsControlOfSourceCreatureEffect()));
    }
}
