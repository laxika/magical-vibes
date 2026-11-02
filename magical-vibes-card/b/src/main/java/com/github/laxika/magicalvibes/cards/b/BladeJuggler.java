package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "63")
@CardRegistration(set = "RVR", collectorNumber = "67")
public class BladeJuggler extends Card {

    public BladeJuggler() {
        // Spectacle {2}{B}
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{2}{B}")),
                new OpponentLostLifeThisTurn(1),
                false));

        // When this creature enters, it deals 1 damage to you and you draw a card.
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER),
                new DrawCardEffect(1)));
    }
}
