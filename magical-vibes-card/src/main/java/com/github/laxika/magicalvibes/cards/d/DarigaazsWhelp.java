package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostDrawnDragonEffect;
import com.github.laxika.magicalvibes.model.effect.SeekDragonAndPerpetuallyBoostEffect;

@CardRegistration(set = "YDMU", collectorNumber = "8")
public class DarigaazsWhelp extends Card {

    public DarigaazsWhelp() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{1}{R}"));
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new PerpetuallyBoostDrawnDragonEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new Kicked(), new SeekDragonAndPerpetuallyBoostEffect()));
    }
}
