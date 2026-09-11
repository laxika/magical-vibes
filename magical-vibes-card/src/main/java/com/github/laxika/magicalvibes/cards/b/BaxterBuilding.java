package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "261")
public class BaxterBuilding extends Card {

    public BaxterBuilding() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new AwardAnyColorManaEffect(4, true)),
                "{4}, {T}: Add four mana in any combination of colors."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new DrawCardEffect(1)),
                "{4}, {T}: Draw a card. Activate only if you control a creature with toughness 4 or greater."
        ).withRequiredControlledPermanents(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentToughnessAtLeastPredicate(4)
                )),
                1,
                "a creature with toughness 4 or greater"
        ));
    }
}
