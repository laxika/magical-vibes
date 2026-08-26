package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "209")
public class EliteHeadhunter extends Card {

    public EliteHeadhunter() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B/R}{B/R}{B/R}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsArtifactPredicate()
                                )),
                                "another creature or an artifact"
                        ),
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(2)
                ),
                "{B/R}{B/R}{B/R}, Sacrifice another creature or an artifact: Elite Headhunter deals 2 damage to target creature or planeswalker."
        ));
    }
}
