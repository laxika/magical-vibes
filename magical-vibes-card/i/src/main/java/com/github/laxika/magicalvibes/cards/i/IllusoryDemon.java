package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "21")
public class IllusoryDemon extends Card {

    public IllusoryDemon() {
        // Flying (auto-loaded from Scryfall).
        // When you cast a spell, sacrifice this creature.
        addTrigger(Triggers.youCastSpell().anySpell(), new SacrificeSelfEffect());
    }
}
