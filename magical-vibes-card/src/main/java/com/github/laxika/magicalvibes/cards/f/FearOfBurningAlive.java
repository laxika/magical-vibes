package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.Delirium;

@CardRegistration(set = "DSK", collectorNumber = "135")
public class FearOfBurningAlive extends Card {

    public FearOfBurningAlive() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DealDamageToPlayersEffect(4, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT,
                new ConditionalEffect(new Delirium(),
                        new DealDamageToTargetCreatureDamagedPlayerControlsEffect(new EventValue())));
    }
}
