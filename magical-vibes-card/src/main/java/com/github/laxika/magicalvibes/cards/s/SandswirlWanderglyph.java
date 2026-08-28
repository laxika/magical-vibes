package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentCantAttackSourceControllerThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsIfAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

public class SandswirlWanderglyph extends Card {

    public SandswirlWanderglyph() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new SpellCastTriggerEffect(null,
                        List.of(new OpponentCantAttackSourceControllerThisTurnEffect()), true));
        addEffect(EffectSlot.STATIC, new OpponentsCantCastSpellsIfAttackedThisTurnEffect(true));
    }
}
