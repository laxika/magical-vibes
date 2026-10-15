package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;

@CardRegistration(set = "PIP", collectorNumber = "59")
@CardRegistration(set = "PIP", collectorNumber = "587")
public class IanTheReckless extends Card {

    public IanTheReckless() {
        addEffect(EffectSlot.ON_ATTACK, new TriggeringPermanentConditionalEffect(
                new PermanentIsModifiedPredicate(),
                new MayEffect(
                        SequenceEffect.of(
                                new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.CONTROLLER),
                                new DealDamageToAnyTargetEffect(new SourcePower())),
                        "Have Ian the Reckless deal damage equal to its power to you and any target?")));
    }
}
