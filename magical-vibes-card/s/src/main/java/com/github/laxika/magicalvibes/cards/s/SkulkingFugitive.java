package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "161")
public class SkulkingFugitive extends Card {

    public SkulkingFugitive() {
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(), new SacrificeSelfEffect());
    }
}
