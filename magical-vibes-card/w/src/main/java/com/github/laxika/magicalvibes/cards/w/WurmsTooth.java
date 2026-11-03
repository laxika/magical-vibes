package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "9ED", collectorNumber = "316")
@CardRegistration(set = "10E", collectorNumber = "346")
@CardRegistration(set = "M10", collectorNumber = "222")
@CardRegistration(set = "M11", collectorNumber = "222")
@CardRegistration(set = "M12", collectorNumber = "223")
@CardRegistration(set = "DST", collectorNumber = "162")
@CardRegistration(set = "DPA", collectorNumber = "97")
public class WurmsTooth extends Card {

    public WurmsTooth() {
        addTrigger(Triggers.playerCastsSpell().withFilter(new CardColorPredicate(CardColor.GREEN)),
                new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
    }
}
