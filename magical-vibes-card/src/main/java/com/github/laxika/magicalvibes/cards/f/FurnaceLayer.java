package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RandomPlayerDiscardsAndLosesLifeIfLandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OPC2", collectorNumber = "14")
public class FurnaceLayer extends Card {

    public FurnaceLayer() {
        RandomPlayerDiscardsAndLosesLifeIfLandEffect discardEffect =
                new RandomPlayerDiscardsAndLosesLifeIfLandEffect();
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, discardEffect);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, discardEffect);

        target(TargetFilters.nonlandPermanent()).addEffect(EffectSlot.CHAOS_TRIGGERED,
                new MayEffect(new DestroyTargetPermanentEffect(), "Destroy target nonland permanent?"));
    }
}
