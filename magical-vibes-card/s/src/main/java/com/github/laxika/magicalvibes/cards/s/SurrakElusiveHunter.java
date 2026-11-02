package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "161")
public class SurrakElusiveHunter extends Card {

    public SurrakElusiveHunter() {
        addStaticEffect(new CantBeCounteredEffect());
        addTrigger(Triggers.creatureOrCreatureSpellYouControlBecomesTargetOfOpponentSpellOrAbility(),
                new DrawCardEffect());
    }
}
