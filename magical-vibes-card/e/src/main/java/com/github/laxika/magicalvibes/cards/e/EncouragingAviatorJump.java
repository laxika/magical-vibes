package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.j.Jump;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/**
 * Encouraging Aviator // Jump (SOS 46).
 */
@CardRegistration(set = "SOS", collectorNumber = "46")
public class EncouragingAviatorJump extends Card {

    public EncouragingAviatorJump() {
        setBackFaceCard(new Jump());
        addEffect(EffectSlot.ON_ATTACK, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "Jump";
    }
}
