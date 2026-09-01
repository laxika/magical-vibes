package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SNC", collectorNumber = "189")
public class ForgeBoss extends Card {

    public ForgeBoss() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new OncePerTurnTriggerEffect(new TriggeringPermanentConditionalEffect(
                        new PermanentIsCreaturePredicate(),
                        new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT)
                )));
    }
}
