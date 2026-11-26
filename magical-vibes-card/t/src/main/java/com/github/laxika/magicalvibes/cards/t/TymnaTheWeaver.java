package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.OpponentDealtCombatDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.OpponentsDealtCombatDamageThisTurn;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FCA", collectorNumber = "18")
@CardRegistration(set = "C16", collectorNumber = "48")
public class TymnaTheWeaver extends Card {

    public TymnaTheWeaver() {
        // At the beginning of each of your postcombat main phases, you may pay X life, where X is the number of
        // opponents that were dealt combat damage this turn. If you do, draw X cards. With X = 0 nothing is asked.
        addTrigger(Triggers.postcombatMainPhase(ActivePlayer.YOU),
                new ConditionalEffect(new OpponentDealtCombatDamageThisTurn(),
                        UnlessPaysEffect.youMayPayLife(new OpponentsDealtCombatDamageThisTurn(),
                                new DrawCardEffect(new OpponentsDealtCombatDamageThisTurn()))));
    }
}
