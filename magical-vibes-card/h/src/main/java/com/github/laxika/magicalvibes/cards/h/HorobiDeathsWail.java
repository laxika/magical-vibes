package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "117")
@CardRegistration(set = "MUL", collectorNumber = "14")
@CardRegistration(set = "MUL", collectorNumber = "79")
@CardRegistration(set = "MUL", collectorNumber = "144")
public class HorobiDeathsWail extends Card {

    public HorobiDeathsWail() {
        // Whenever a creature becomes the target of a spell or ability, destroy that creature.
        // The targeted creature is set as the non-targeting targetId.
        addTrigger(Triggers.creatureBecomesTargetOfSpellOrAbility(),
                new DestroyTargetPermanentEffect(false));
    }
}
