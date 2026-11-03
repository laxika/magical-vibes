package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OpponentCantAttackSourceControllerThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsIfAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


public class SandswirlWanderglyph extends Card {

    public SandswirlWanderglyph() {
        addTrigger(Triggers.opponentCastsSpell().duringOpponentsTurn(),
                new OpponentCantAttackSourceControllerThisTurnEffect());
        addStaticEffect(new OpponentsCantCastSpellsIfAttackedThisTurnEffect(true));
    }
}
