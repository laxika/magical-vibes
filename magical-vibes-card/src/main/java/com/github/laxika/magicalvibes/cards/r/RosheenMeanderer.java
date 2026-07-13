package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "214")
public class RosheenMeanderer extends Card {

    public RosheenMeanderer() {
        // {T}: Add {C}{C}{C}{C}. Spend this mana only on costs that contain {X}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(ManaColor.COLORLESS, 4, new ManaRestriction.XCosts())),
                "{T}: Add {C}{C}{C}{C}. Spend this mana only on costs that contain {X}."
        ));
    }
}
