package com.github.laxika.magicalvibes.cards.b;

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

@CardRegistration(set = "DSK", collectorNumber = "256")
public class BlazemireVerge extends Card {

    public BlazemireVerge() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.RED)),
                "{T}: Add {R}. Activate only if you control a Swamp or a Mountain."
        ).withActivationCondition(
                new AnyOf(List.of(
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.SWAMP)),
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN))
                )),
                "Activate only if you control a Swamp or a Mountain"
        ));
    }
}
