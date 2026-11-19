package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "TSP", collectorNumber = "187")
@CardRegistration(set = "TSR", collectorNumber = "198")
@CardRegistration(set = "C16", collectorNumber = "138")
public class WheelOfFate extends Card {

    public WheelOfFate() {
        addSpellEffect(new DiscardHandEffect(Subjects.eachPlayer()));
        addSpellEffect(new EachPlayerDrawsCardEffect(7));
        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(),
                "Suspend 4\u2014{1}{R}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(4));
    }
}
