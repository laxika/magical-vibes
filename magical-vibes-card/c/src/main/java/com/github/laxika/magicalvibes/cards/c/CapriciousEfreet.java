package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyOneOfTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M10", collectorNumber = "131")
@CardRegistration(set = "C13", collectorNumber = "102")
public class CapriciousEfreet extends Card {

    public CapriciousEfreet() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new DestroyOneOfTargetsAtRandomEffect());
    }
}
