package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OmenMachineDrawStepEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCannotDrawCardsEffect;

@CardRegistration(set = "NPH", collectorNumber = "148")
public class OmenMachine extends Card {

    public OmenMachine() {
        addEffect(EffectSlot.STATIC, new PlayersCannotDrawCardsEffect());
        addEffect(EffectSlot.EACH_DRAW_TRIGGERED, new OmenMachineDrawStepEffect());
    }
}
