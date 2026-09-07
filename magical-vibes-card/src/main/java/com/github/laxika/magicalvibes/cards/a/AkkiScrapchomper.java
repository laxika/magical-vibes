package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "130")
public class AkkiScrapchomper extends Card {

    public AkkiScrapchomper() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsLandPredicate()
                                )),
                                "an artifact or land"),
                        new DrawCardEffect(1)
                ),
                "{1}{R}, {T}, Sacrifice an artifact or land: Draw a card."
        ));
    }
}
