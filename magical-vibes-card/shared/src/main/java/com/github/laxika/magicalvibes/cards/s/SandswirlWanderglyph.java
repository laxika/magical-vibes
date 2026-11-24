package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsIfAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class SandswirlWanderglyph extends Card {

    public SandswirlWanderglyph() {
        addTrigger(Triggers.opponentCastsSpell().duringOpponentsTurn(),
                new CreaturesCantAttackControllerUntilNextTurnEffect(Subjects.triggeringPlayer(), Subjects.you(),
                        true, Duration.UNTIL_END_OF_TURN));
        addStaticEffect(new OpponentsCantCastSpellsIfAttackedThisTurnEffect(true));
    }
}
