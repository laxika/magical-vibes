package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsWithManaValueUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


public class LivingBreakthrough extends Card {

    public LivingBreakthrough() {
        addTrigger(Triggers.youCastSpell().anySpell(), new OpponentsCantCastSpellsWithManaValueUntilNextTurnEffect());
    }
}
