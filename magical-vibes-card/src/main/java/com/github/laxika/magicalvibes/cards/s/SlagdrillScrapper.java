package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "160")
public class SlagdrillScrapper extends Card {

    public SlagdrillScrapper() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsLandPredicate())),
                                "another artifact or land"),
                        new DrawCardEffect(1)
                ),
                "{2}, {T}, Sacrifice another artifact or land: Draw a card."
        ));
    }
}
