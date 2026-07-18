package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.CameUnderControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "158")
@CardRegistration(set = "4ED", collectorNumber = "135")
public class ErgRaiders extends Card {

    public ErgRaiders() {
        // At the beginning of your end step, if this creature didn't attack this turn, it deals
        // 2 damage to you unless it came under your control this turn.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new AllOf(List.of(new DidntAttack(), new NotCondition(new CameUnderControlThisTurn()))),
                new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER)));
    }
}
