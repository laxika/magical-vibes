package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "AVR", collectorNumber = "123")
public class UndeadExecutioner extends Card {

    public UndeadExecutioner() {
        // When this creature dies, you may have target creature get -2/-2 until end of turn.
        // The death pipeline picks the creature target as the trigger is stacked (CR 603.3d) and
        // defaults to creatures only, so no explicit target filter is needed.
        addEffect(EffectSlot.ON_DEATH, new MayEffect(new BoostTargetCreatureEffect(-2, -2),
                "Have target creature get -2/-2 until end of turn?"));
    }
}
