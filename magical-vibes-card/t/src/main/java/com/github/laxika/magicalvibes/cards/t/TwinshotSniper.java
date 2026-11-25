package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "168")
public class TwinshotSniper extends Card {

    public TwinshotSniper() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), DealDamageEffect.toAnyTarget(2));

        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(DealDamageEffect.toAnyTarget(2)),
                "Channel — {1}{R}, Discard this card: It deals 2 damage to any target."
        ));
    }
}
