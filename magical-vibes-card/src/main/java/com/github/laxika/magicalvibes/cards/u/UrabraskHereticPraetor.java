package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawExileTopCardMayPlayThisTurnEffect;

@CardRegistration(set = "SNC", collectorNumber = "129")
public class UrabraskHereticPraetor extends Card {

    public UrabraskHereticPraetor() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ExileTopCardMayPlayThisTurnEffect(false));
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED,
                new RegisterNextDrawExileTopCardMayPlayThisTurnEffect());
    }
}
