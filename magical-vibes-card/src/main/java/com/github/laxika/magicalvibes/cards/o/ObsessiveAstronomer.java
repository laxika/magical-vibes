package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeDayAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;

@CardRegistration(set = "MID", collectorNumber = "152")
public class ObsessiveAstronomer extends Card {

    public ObsessiveAstronomer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeDayAsEntersEffect());
        addEffect(EffectSlot.ON_DAY_NIGHT_CHANGE, new DiscardUpToThenDrawThatManyEffect(2));
    }
}
