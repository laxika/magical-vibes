package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandOfDamagedPlayerMayCastForLifeThisTurnEffect;

@CardRegistration(set = "M3C", collectorNumber = "51")
public class BismuthMindrender extends Card {

    public BismuthMindrender() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileTopUntilNonlandOfDamagedPlayerMayCastForLifeThisTurnEffect());
    }
}
