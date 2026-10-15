package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastOrActivateDuringYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

public class TheSensationalSheHulk extends Card {

    public TheSensationalSheHulk() {
        addEffect(EffectSlot.STATIC, new OpponentsCantCastOrActivateDuringYourTurnEffect(false));
        addEffect(EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE, new TriggeringPermanentConditionalEffect(
                new PermanentControlledBySourceControllerPredicate(),
                new OncePerTurnTriggerEffect(new MayEffect(
                        new DealDamageToAnyTargetEffect(new EventValue()),
                        "Have The Sensational She-Hulk deal that much damage to any target?"))));
    }
}
