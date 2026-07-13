package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

/**
 * Syphon Soul — {2}{B} Sorcery
 *
 * Syphon Soul deals 2 damage to each other player. You gain life equal to the damage dealt this way.
 */
@CardRegistration(set = "6ED", collectorNumber = "159")
public class SyphonSoul extends Card {

    public SyphonSoul() {
        // Deal 2 damage to each other player, then gain life equal to the damage dealt.
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
    }
}
