package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureDamagedPlayerControlsEffect;

@CardRegistration(set = "BNG", collectorNumber = "108")
public class SatyrFiredancer extends Card {

    public SatyrFiredancer() {
        addEffect(EffectSlot.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE,
                new DealDamageToTargetCreatureDamagedPlayerControlsEffect(new EventValue()));
    }
}
