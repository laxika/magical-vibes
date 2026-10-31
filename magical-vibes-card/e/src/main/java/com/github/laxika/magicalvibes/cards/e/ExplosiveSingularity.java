package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesForManaCost;

@CardRegistration(set = "NEO", collectorNumber = "140")
public class ExplosiveSingularity extends Card {

    public ExplosiveSingularity() {
        addCastingCost(new TapCreaturesForManaCost());
        addSpellEffect(new DealDamageToAnyTargetEffect(10));
    }
}
