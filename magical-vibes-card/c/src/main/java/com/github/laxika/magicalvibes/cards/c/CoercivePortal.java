package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CoercivePortalEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VMA", collectorNumber = "266")
@CardRegistration(set = "LCC", collectorNumber = "109")
public class CoercivePortal extends Card {

    public CoercivePortal() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CoercivePortalEffect());
    }
}
