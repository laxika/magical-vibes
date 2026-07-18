package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "5ED", collectorNumber = "353")
@CardRegistration(set = "4ED", collectorNumber = "297")
public class BatteringRam extends Card {

    public BatteringRam() {
        // "At the beginning of combat on your turn, this creature gains banding until end of combat."
        // Banding is not modeled by the engine, so that ability is intentionally omitted.

        // Whenever this creature becomes blocked by a Wall, destroy that Wall at end of combat.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new DestroyCombatOpponentAtEndOfCombatEffect(new PermanentHasSubtypePredicate(CardSubtype.WALL), false),
                TriggerMode.PER_BLOCKER);
    }
}
