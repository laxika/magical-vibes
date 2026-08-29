package com.github.laxika.magicalvibes.cards.s;

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

@CardRegistration(set = "DFT", collectorNumber = "264")
public class SunbillowVerge extends Card {

    public SunbillowVerge() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.RED)),
                "{T}: Add {R}. Activate only if you control a Mountain or a Plains."
        ).withActivationCondition(
                new AnyOf(List.of(
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)),
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.PLAINS))
                )),
                "Activate only if you control a Mountain or a Plains"
        ));
    }
}
