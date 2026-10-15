package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachCreatureDamagedPlayerControlsEffect;

@CardRegistration(set = "FRF", collectorNumber = "114")
public class ShockmawDragon extends Card {

    public ShockmawDragon() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DealDamageToEachCreatureDamagedPlayerControlsEffect(1));
    }
}
