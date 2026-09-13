package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsWithManaValueUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

public class LivingBreakthrough extends Card {

    public LivingBreakthrough() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null, List.of(new OpponentsCantCastSpellsWithManaValueUntilNextTurnEffect())));
    }
}
