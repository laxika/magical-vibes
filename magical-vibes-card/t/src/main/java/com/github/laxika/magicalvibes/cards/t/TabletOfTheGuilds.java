package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeForEachChosenColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "235")
public class TabletOfTheGuilds extends Card {

    public TabletOfTheGuilds() {
        addStaticEffect(new ChooseColorOnEnterEffect(2));
        addTrigger(Triggers.youCastSpell(), new GainLifeForEachChosenColorSpellCastEffect());
    }
}
