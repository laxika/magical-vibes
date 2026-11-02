package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OpponentCantAttackSourceControllerThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsIfAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class SandswirlWanderglyph extends Card {

    public SandswirlWanderglyph() {
        addTrigger(Triggers.opponentCastsSpell(),
                new SpellCastTriggerEffect(null,
                        List.of(new OpponentCantAttackSourceControllerThisTurnEffect()), true));
        addStaticEffect(new OpponentsCantCastSpellsIfAttackedThisTurnEffect(true));
    }
}
