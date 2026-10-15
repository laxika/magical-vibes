package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HOB", collectorNumber = "67")
public class FrontPorchSentries extends Card {

    public FrontPorchSentries() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_DEATH, new BoostTargetCreatureEffect(-1, -1));
    }
}
