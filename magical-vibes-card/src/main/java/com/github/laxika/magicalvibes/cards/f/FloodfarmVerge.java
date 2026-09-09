package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "259")
public class FloodfarmVerge extends Card {

    public FloodfarmVerge() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLUE)),
                "{T}: Add {U}. Activate only if you control a Plains or an Island."
        ).withActivationCondition(
                new AnyOf(List.of(
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.PLAINS)),
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND))
                )),
                "Activate only if you control a Plains or an Island"
        ));
    }
}
