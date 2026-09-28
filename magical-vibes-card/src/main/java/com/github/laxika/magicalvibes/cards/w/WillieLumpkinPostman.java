package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamagedPlayerMayDrawAndCantAttackControllerUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

@CardRegistration(set = "MSC", collectorNumber = "101")
@CardRegistration(set = "MSC", collectorNumber = "425")
public class WillieLumpkinPostman extends Card {

    public WillieLumpkinPostman() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, SequenceEffect.of(
                new DrawCardEffect(),
                new DamagedPlayerMayDrawAndCantAttackControllerUntilNextTurnEffect()));
    }
}
