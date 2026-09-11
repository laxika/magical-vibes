package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "144")
@CardRegistration(set = "ONS", collectorNumber = "230")
@CardRegistration(set = "DD1", collectorNumber = "51")
@CardRegistration(set = "DDG", collectorNumber = "49")
@CardRegistration(set = "VMA", collectorNumber = "186")
@CardRegistration(set = "EVG", collectorNumber = "51")
public class SkirkProspector extends Card {

    public SkirkProspector() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificePermanentCost(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)
                        )),
                        "Sacrifice a Goblin",
                        false
                ), new AwardManaEffect(ManaColor.RED)),
                "Sacrifice a Goblin: Add {R}."
        ));
    }
}
