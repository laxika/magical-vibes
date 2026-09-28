package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "NCC", collectorNumber = "34")
@CardRegistration(set = "NCC", collectorNumber = "135")
public class BodyCount extends Card {

    public BodyCount() {
        // Spectacle {B}
        addCastingOption(AlternateHandCast.spectacle("{B}", new OpponentLostLifeThisTurn(1)));

        // Draw a card for each creature that died under your control this turn.
        addEffect(EffectSlot.SPELL,
                new DrawCardEffect(new CreatureDeathsThisTurn(CountScope.CONTROLLER)));
    }
}
