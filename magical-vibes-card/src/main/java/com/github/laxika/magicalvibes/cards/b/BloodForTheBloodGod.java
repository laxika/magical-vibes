package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "40K", collectorNumber = "108")
public class BloodForTheBloodGod extends Card {

    public BloodForTheBloodGod() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new CreatureDeathsThisTurn(CountScope.ANY_PLAYER)));
        addEffect(EffectSlot.SPELL, new DiscardHandEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(8));
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(8, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
