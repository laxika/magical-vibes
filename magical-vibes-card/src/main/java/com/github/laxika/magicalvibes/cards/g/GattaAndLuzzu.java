package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIC", collectorNumber = "19")
@CardRegistration(set = "FIC", collectorNumber = "134")
public class GattaAndLuzzu extends Card {

    public GattaAndLuzzu() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        PreventDamageEffect.allToTargetCreaturesAndAddPlusOnePlusOneCounters(
                                new PermanentIsCreaturePredicate()));
    }
}
