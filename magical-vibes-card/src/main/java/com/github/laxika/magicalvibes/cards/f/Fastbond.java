package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerPlayedAtLeastLandsThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "SUM", collectorNumber = "194")
public class Fastbond extends Card {

    public Fastbond() {
        // You may play any number of lands on each of your turns.
        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(Integer.MAX_VALUE - 1));

        // Whenever you play a land, if it wasn't the first land you played this turn, this enchantment
        // deals 1 damage to you. The outer wrapper checks the intervening-if when the land-play
        // trigger is collected, after this land has been counted.
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND, new TriggeringCardConditionalEffect(
                new CardTruePredicate(),
                new ConditionalEffect(
                        new ControllerPlayedAtLeastLandsThisTurn(2),
                        new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER))));
    }
}
