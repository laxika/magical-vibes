package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "210")
public class SeekerOfSunlight extends Card {

    public SeekerOfSunlight() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new ExploreEffect()),
                "{2}{G}: This creature explores. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
