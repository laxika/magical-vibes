package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "170")
public class RiftSower extends Card {

    public RiftSower() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color."
        ));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(),
                "Suspend 2—{G}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(2));
    }
}
