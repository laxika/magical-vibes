package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "84")
public class FurnaceScamp extends Card {

    public FurnaceScamp() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(SequenceEffect.of(
                        new SacrificeSelfEffect(),
                        new ConditionalEffect(HappenedThisWay.sacrificed(),
                                new DealDamageToPlayersEffect(3, Subjects.triggeringPlayer()))),
                        "You may sacrifice it. If you do, it deals 3 damage to that player."));
    }
}
