package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "TSP", collectorNumber = "117")
public class ManaSkimmer extends Card {

    public ManaSkimmer() {
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER,
                TapChosenPermanentEffect.damagedPlayerControlsAndSkipsNextUntap(
                        new PermanentIsLandPredicate()));
    }
}
