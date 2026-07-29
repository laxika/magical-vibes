package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "MIR", collectorNumber = "311")
public class MisersCage extends Card {

    public MisersCage() {
        // "At the beginning of each opponent's upkeep, if that player has five or more cards in
        // hand, this artifact deals 2 damage to that player."
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new ConditionalEffect(
                new ActivePlayerHandAtLeast(5),
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER)));
    }
}
