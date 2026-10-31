package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
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
        addStaticEffect(new ReduceOwnCastCostEffect(
                new CreatureDeathsThisTurn(CountScope.ANY_PLAYER)));
        addSpellEffect(new DiscardHandEffect());
        addSpellEffect(new DrawCardEffect(8));
        addSpellEffect(new DealDamageToPlayersEffect(8, DamageRecipient.EACH_OPPONENT));
        addSpellEffect(new ExileSpellEffect());
    }
}
