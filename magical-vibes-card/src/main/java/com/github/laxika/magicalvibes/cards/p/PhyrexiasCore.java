package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "165")
public class PhyrexiasCore extends Card {

    public PhyrexiasCore() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {1}, {T}, Sacrifice an artifact: You gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false), new GainLifeEffect(1)),
                "{1}, {T}, Sacrifice an artifact: You gain 1 life."
        ));
    }
}
