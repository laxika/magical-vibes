package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "203")
public class WildDefiance extends Card {

    public WildDefiance() {
        // Whenever a creature you control becomes the target of an instant or sorcery spell,
        // that creature gets +3/+3 until end of turn. The targeted creature is set as the
        // non-targeting targetId, so the boost applies to "that creature".
        addTrigger(Triggers.creatureYouControlBecomesTargetOfInstantOrSorcerySpell(),
                new BoostTargetCreatureEffect(3, 3));
    }
}
