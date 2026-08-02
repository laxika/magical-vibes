package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;

@CardRegistration(set = "CHK", collectorNumber = "117")
public class HorobiDeathsWail extends Card {

    public HorobiDeathsWail() {
        // Whenever a creature becomes the target of a spell or ability, destroy that creature.
        // The targeted creature is set as the non-targeting targetId.
        addEffect(EffectSlot.ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
                new DestroyTargetPermanentEffect(false));
    }
}
