package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAnyNumberEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "140")
@CardRegistration(set = "SLD", collectorNumber = "857")
@CardRegistration(set = "DMC", collectorNumber = "125")
public class NehebDreadhordeChampion extends Card {

    public NehebDreadhordeChampion() {
        addTrigger(Triggers.dealsCombatDamageToPlayerOrPlaneswalker(),
                SequenceEffect.of(
                        new DiscardAnyNumberEffect(),
                        new DrawCardEffect(new EventValue()),
                        new AwardPersistentManaEffect(ManaColor.RED, new EventValue())));
    }
}
