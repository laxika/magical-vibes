package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsWithManaValueUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class LivingBreakthrough extends Card {

    public LivingBreakthrough() {
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                null, List.of(new OpponentsCantCastSpellsWithManaValueUntilNextTurnEffect())));
    }
}
