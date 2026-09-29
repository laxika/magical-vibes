package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetExiledCardOwnedByDamagedPlayerOnBottomOfOwnersLibraryAndGainLifeEffect;

@CardRegistration(set = "WHO", collectorNumber = "71")
public class TimeReaper extends Card {

    public TimeReaper() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new PutTargetExiledCardOwnedByDamagedPlayerOnBottomOfOwnersLibraryAndGainLifeEffect(3));
    }
}
