package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GandalfWestwardVoyagerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "6")
@CardRegistration(set = "LTC", collectorNumber = "89")
@CardRegistration(set = "LTC", collectorNumber = "463")
public class GandalfWestwardVoyager extends Card {

    public GandalfWestwardVoyager() {
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardMinManaValuePredicate(5, true),
                List.of(new GandalfWestwardVoyagerEffect())));
    }
}
