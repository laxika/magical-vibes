package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "ORI", collectorNumber = "144")
@CardRegistration(set = "RAV", collectorNumber = "122")
@CardRegistration(set = "PC2", collectorNumber = "42")
@CardRegistration(set = "PCA", collectorNumber = "42")
public class FieryConclusion extends Card {

    public FieryConclusion() {
        addCastingCost(new SacrificeCreatureCost());
        addSpellEffect(new DealDamageToTargetCreatureEffect(5));
    }
}
