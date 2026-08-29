package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyAttachmentsOnTargetCreatureEffect;

@CardRegistration(set = "ULG", collectorNumber = "114")
public class TreefolkMystic extends Card {

    public TreefolkMystic() {
        addEffect(EffectSlot.ON_BLOCK, new DestroyAttachmentsOnTargetCreatureEffect(true, false));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new DestroyAttachmentsOnTargetCreatureEffect(true, false), TriggerMode.PER_BLOCKER);
    }
}
