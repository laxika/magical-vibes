package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDyingCreatureToughnessEffect;

@CardRegistration(set = "MIR", collectorNumber = "265")
public class GrimFeast extends Card {

    public GrimFeast() {
        // "At the beginning of your upkeep, this enchantment deals 1 damage to you."
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));

        // "Whenever a creature is put into an opponent's graveyard from the battlefield, you gain
        // life equal to its toughness." The toughness used is the creature's last-known effective
        // toughness on the battlefield, snapshotted when the trigger is collected.
        addEffect(EffectSlot.ON_PERMANENT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
                new GainLifeEqualToDyingCreatureToughnessEffect());
    }
}
