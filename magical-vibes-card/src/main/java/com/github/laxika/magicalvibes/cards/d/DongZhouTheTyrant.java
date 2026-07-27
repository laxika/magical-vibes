package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;


@CardRegistration(set = "PTK", collectorNumber = "109")
public class DongZhouTheTyrant extends Card {

    public DongZhouTheTyrant() {
        // When Dong Zhou enters, target creature an opponent controls deals damage equal to its
        // power to that player.
        target(TargetFilters.creatureAnOpponentControls()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new TargetCreatureDealsPowerDamageToControllerEffect());
    }
}
