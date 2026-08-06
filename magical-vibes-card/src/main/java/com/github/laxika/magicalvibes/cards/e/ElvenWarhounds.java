package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;

@CardRegistration(set = "TMP", collectorNumber = "225")
public class ElvenWarhounds extends Card {

    public ElvenWarhounds() {
        // Whenever this creature becomes blocked by a creature, put that creature on top of its
        // owner's library. Fires once per blocker; the blocker is carried as the trigger's
        // (non-targeting) target.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new PutTargetOnTopOfLibraryEffect(), TriggerMode.PER_BLOCKER);
    }
}
