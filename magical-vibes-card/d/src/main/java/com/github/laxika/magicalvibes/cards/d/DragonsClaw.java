package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "322")
@CardRegistration(set = "M10", collectorNumber = "210")
@CardRegistration(set = "M11", collectorNumber = "205")
@CardRegistration(set = "M12", collectorNumber = "206")
@CardRegistration(set = "9ED", collectorNumber = "296")
@CardRegistration(set = "DST", collectorNumber = "117")
@CardRegistration(set = "DPA", collectorNumber = "92")
@CardRegistration(set = "DDG", collectorNumber = "63")
public class DragonsClaw extends Card {

    public DragonsClaw() {
        addTrigger(Triggers.playerCastsSpell().withFilter(new CardColorPredicate(CardColor.RED)),
                new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
    }
}
