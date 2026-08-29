package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsSequenceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "66")
public class EaterOfHope extends Card {

    public EaterOfHope() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new RegenerateEffect()
                ),
                "{B}, Sacrifice another creature: Regenerate this creature."
        ));

        var anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new SacrificePermanentsSequenceCost(
                                List.of(anotherCreature, anotherCreature),
                                List.of("another creature", "another creature")
                        ),
                        new DestroyTargetPermanentEffect()
                ),
                "{2}{B}, Sacrifice two other creatures: Destroy target creature.",
                TargetFilters.creature()
        ));
    }
}
