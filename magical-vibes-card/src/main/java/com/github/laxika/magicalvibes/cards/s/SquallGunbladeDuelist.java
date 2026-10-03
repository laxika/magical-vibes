package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.PlayerAttacksOneOfYourOpponentsWithPowerOrToughnessEqualToSourceChosenNumber;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAttackedTargetEffect;

@CardRegistration(set = "FIC", collectorNumber = "453")
public class SquallGunbladeDuelist extends Card {

    public SquallGunbladeDuelist() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseNumberOnEnterEffect(0, 20));
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(
                        new PlayerAttacksOneOfYourOpponentsWithPowerOrToughnessEqualToSourceChosenNumber(),
                        new DealDamageToAttackedTargetEffect(new SourcePower())));
    }
}
