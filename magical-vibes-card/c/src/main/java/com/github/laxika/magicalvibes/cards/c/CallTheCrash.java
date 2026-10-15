package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsToBattlefieldEffect;

import java.util.List;

@CardRegistration(set = "YTDM", collectorNumber = "19")
public class CallTheCrash extends Card {

    public CallTheCrash() {
        addEffect(EffectSlot.SPELL, new ConjureCardsToBattlefieldEffect("Siege Rhino", new Fixed(2)));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}{B}{G}",
                List.of(),
                "Suspend 2—{1}{W}{B}{G}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(2));
    }
}
