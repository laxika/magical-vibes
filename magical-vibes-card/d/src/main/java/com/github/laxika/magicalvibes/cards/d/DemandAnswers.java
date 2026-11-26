package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MKM", collectorNumber = "122")
@CardRegistration(set = "MKM", collectorNumber = "306")
public class DemandAnswers extends Card {

    public DemandAnswers() {
        addCastingCost(AnyOfCost.of(new DiscardCardTypeCost(null, null),
                new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false)));
        addSpellEffect(new DrawCardEffect(2));
    }
}
