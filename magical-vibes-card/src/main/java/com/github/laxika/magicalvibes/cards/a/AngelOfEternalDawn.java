package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TurnsTakenByController;
import com.github.laxika.magicalvibes.model.effect.BecomeDayEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsWithManaValueGreaterThanEffect;

@CardRegistration(set = "YMID", collectorNumber = "1")
public class AngelOfEternalDawn extends Card {

    public AngelOfEternalDawn() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeDayEffect());
        addEffect(EffectSlot.STATIC, new OpponentsCantCastSpellsWithManaValueGreaterThanEffect(
                new TurnsTakenByController()));
    }
}
