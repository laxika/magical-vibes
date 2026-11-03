package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "APC", collectorNumber = "78")
public class GladeGnarr extends Card {

    public GladeGnarr() {
        addTrigger(Triggers.playerCastsSpell().withFilter(new CardColorPredicate(CardColor.BLUE)),
                new BoostSelfEffect(2, 2));
    }
}
