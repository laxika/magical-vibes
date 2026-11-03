package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "9ED", collectorNumber = "302")
@CardRegistration(set = "10E", collectorNumber = "329")
@CardRegistration(set = "M10", collectorNumber = "213")
@CardRegistration(set = "M11", collectorNumber = "210")
@CardRegistration(set = "M12", collectorNumber = "211")
@CardRegistration(set = "DST", collectorNumber = "126")
@CardRegistration(set = "DPA", collectorNumber = "93")
public class KrakensEye extends Card {

    public KrakensEye() {
        addTrigger(Triggers.playerCastsSpell().withFilter(new CardColorPredicate(CardColor.BLUE)),
                new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
    }
}
