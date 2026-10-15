package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerIsMonarch;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageDoesNotCauseLifeLossEffect;

@CardRegistration(set = "NCC", collectorNumber = "192")
public class ArchonOfCoronation extends Card {

    public ArchonOfCoronation() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeMonarchEffect());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerIsMonarch(), new DamageDoesNotCauseLifeLossEffect()));
    }
}
