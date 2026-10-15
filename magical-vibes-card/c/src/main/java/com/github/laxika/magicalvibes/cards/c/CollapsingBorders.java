package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "INV", collectorNumber = "141")
public class CollapsingBorders extends Card {

    public CollapsingBorders() {
        // At the beginning of each player's upkeep, that player gains 1 life for each basic land
        // type among lands they control, then this enchantment deals 3 damage to that player.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, SequenceEffect.of(
                new GainLifeEffect(
                        new BasicLandTypesAmongControlledLands(CountScope.TARGET_PLAYER),
                        GainLifeRecipient.TRIGGERING_PLAYER),
                new DealDamageToPlayersEffect(3, DamageRecipient.TRIGGERING_PLAYER)));
    }
}
