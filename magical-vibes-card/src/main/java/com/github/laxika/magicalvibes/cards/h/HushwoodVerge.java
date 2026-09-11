package com.github.laxika.magicalvibes.cards.h;

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

@CardRegistration(set = "DSK", collectorNumber = "261")
public class HushwoodVerge extends Card {

    public HushwoodVerge() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.WHITE)),
                "{T}: Add {W}. Activate only if you control a Forest or a Plains."
        ).withActivationCondition(
                new AnyOf(List.of(
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.FOREST)),
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.PLAINS))
                )),
                "Activate only if you control a Forest or a Plains"
        ));
    }
}
