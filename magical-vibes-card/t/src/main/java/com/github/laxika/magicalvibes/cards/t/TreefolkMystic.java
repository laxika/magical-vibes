package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAttachmentsOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ULG", collectorNumber = "114")
public class TreefolkMystic extends Card {

    public TreefolkMystic() {
        addTrigger(Triggers.blocks(), new DestroyAttachmentsOnTargetCreatureEffect(true, false));
        addTrigger(Triggers.becomesBlocked(),
                TriggerOptions.EACH_BLOCKER,
                new DestroyAttachmentsOnTargetCreatureEffect(true, false));
    }
}
