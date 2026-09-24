package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "HA6", collectorNumber = "5")
@CardRegistration(set = "SOC", collectorNumber = "246")
public class LaeliaTheBladeReforged extends Card {

    public LaeliaTheBladeReforged() {
        addEffect(EffectSlot.ON_ATTACK, new ExileTopCardMayPlayThisTurnEffect(false));
        addEffect(EffectSlot.ON_CONTROLLER_CARDS_EXILED_DURING_TURN,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
