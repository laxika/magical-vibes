package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;

import java.util.List;

/**
 * Emeritus of Conflict // Lightning Bolt (SOS 113).
 */
@CardRegistration(set = "SOS", collectorNumber = "113")
public class EmeritusOfConflictLightningBolt extends Card {

    public EmeritusOfConflictLightningBolt() {
        setBackFaceCard(new LightningBolt());

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new NthSpellCastTriggerEffect(3, List.of(new BecomePreparedEffect())));
    }

    @Override
    public String getBackFaceClassName() {
        return "LightningBolt";
    }
}
