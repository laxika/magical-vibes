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

@CardRegistration(set = "DFT", collectorNumber = "250")
public class BleachboneVerge extends Card {

    public BleachboneVerge() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.WHITE)),
                "{T}: Add {W}. Activate only if you control a Plains or a Swamp."
        ).withActivationCondition(
                new AnyOf(List.of(
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.PLAINS)),
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.SWAMP))
                )),
                "Activate only if you control a Plains or a Swamp"
        ));
    }
}
