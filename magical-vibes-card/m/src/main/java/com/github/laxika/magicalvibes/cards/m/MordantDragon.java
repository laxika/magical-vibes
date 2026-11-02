package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "85")
@CardRegistration(set = "DDG", collectorNumber = "58")
@CardRegistration(set = "SCD", collectorNumber = "152")
public class MordantDragon extends Card {

    public MordantDragon() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(new BoostSelfEffect(1, 0)),
                "{1}{R}: This creature gets +1/+0 until end of turn."));

        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(
                        new DealDamageToTargetCreatureDamagedPlayerControlsEffect(new EventValue()),
                        "You may have Mordant Dragon deal that much damage to target creature that player controls."));
    }
}
