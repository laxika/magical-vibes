package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandOfDamagedPlayerMayCounterOrCastEffect;

@CardRegistration(set = "MSH", collectorNumber = "89")
public class BlackWidowSuperSpy extends Card {

    public BlackWidowSuperSpy() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileTopUntilNonlandOfDamagedPlayerMayCounterOrCastEffect());
    }
}
