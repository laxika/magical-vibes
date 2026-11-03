package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "APC", collectorNumber = "76")
public class BogGnarr extends Card {

    public BogGnarr() {
        addTrigger(Triggers.playerCastsSpell().withFilter(new CardColorPredicate(CardColor.BLACK)),
                new BoostSelfEffect(2, 2));
    }
}
