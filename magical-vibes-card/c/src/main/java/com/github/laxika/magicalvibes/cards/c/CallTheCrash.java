package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;

import java.util.List;

@CardRegistration(set = "YTDM", collectorNumber = "19")
public class CallTheCrash extends Card {

    public CallTheCrash() {
        addSpellEffect(ConjureCardsEffect.named("KTK", "200", ConjureDestination.ontoBattlefield()).times(2));
        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(
                false,
                "{1}{W}{B}{G}",
                List.of(),
                "Suspend 2—{1}{W}{B}{G}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(2));
    }
}
