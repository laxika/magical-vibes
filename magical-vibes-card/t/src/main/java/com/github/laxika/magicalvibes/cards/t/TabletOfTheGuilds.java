package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellChosenColorCount;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "235")
public class TabletOfTheGuilds extends Card {

    public TabletOfTheGuilds() {
        addStaticEffect(new ChooseColorOnEnterEffect(2));
        // "If it's at least one of the chosen colors": the filter as the ability triggers, and the count (no life
        // for a spell that is none of them any more) as it resolves.
        addTrigger(Triggers.youCastSpell().withFilter(new CardHasSourceChosenColorPredicate()),
                new GainLifeEffect(new TriggeringSpellChosenColorCount()));
    }
}
