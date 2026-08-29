package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "144")
public class WhispererOfTheWilds extends Card {

    public WhispererOfTheWilds() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN)),
                "{T}: Add {G}."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN, 2)),
                "Ferocious — {T}: Add {G}{G}. Activate only if you control a creature with power 4 or greater."
        ).withActivationCondition(
                new ControlsPermanent(new PermanentPowerAtLeastPredicate(4)),
                "Activate only if you control a creature with power 4 or greater."));
    }
}
