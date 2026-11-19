package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "202")
public class KotisTheFangkeeper extends Card {

    public KotisTheFangkeeper() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ExileTopCardsAndMayCastSpellsEffect(
                        new EventValue(), Subjects.triggeringPlayer(), true, new EventValue()));
    }
}
