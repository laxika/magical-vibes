package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "2X2", collectorNumber = "134")
public class AnnoyedAltisaur extends Card {

    public AnnoyedAltisaur() {
        addTrigger(Triggers.youCastThisSpell(), new CascadeEffect());
    }
}
