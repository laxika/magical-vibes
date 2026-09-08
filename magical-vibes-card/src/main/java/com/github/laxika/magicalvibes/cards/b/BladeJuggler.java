package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "63")
public class BladeJuggler extends Card {

    public BladeJuggler() {
        // Spectacle {2}{B}
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{2}{B}")),
                new OpponentLostLifeThisTurn(1),
                false));

        // When this creature enters, it deals 1 damage to you and you draw a card.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER),
                new DrawCardEffect(1)));
    }
}
