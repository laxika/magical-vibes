package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStormToNextInstantOrSorceryCastThisTurnEffect;

@CardRegistration(set = "OTC", collectorNumber = "25")
@CardRegistration(set = "OTC", collectorNumber = "61")
public class CracklingSpellslinger extends Card {

    public CracklingSpellslinger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new WasCast(), new GrantStormToNextInstantOrSorceryCastThisTurnEffect()));
    }
}
