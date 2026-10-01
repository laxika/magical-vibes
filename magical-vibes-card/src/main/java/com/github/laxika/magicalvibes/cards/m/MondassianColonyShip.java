package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.OtherControlledCreaturesSharingCreatureTypeWithTarget;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TurnTargetCreatureFaceDownEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WHO", collectorNumber = "590")
public class MondassianColonyShip extends Card {

    public MondassianColonyShip() {
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS,
                new BoostTargetCreatureEffect(
                        new OtherControlledCreaturesSharingCreatureTypeWithTarget(),
                        new OtherControlledCreaturesSharingCreatureTypeWithTarget()));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.CHAOS_TRIGGERED, TurnTargetCreatureFaceDownEffect.asCyberman());
    }
}
