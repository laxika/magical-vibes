package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHR", collectorNumber = "85")
@CardRegistration(set = "TSB", collectorNumber = "100")
@CardRegistration(set = "LEG", collectorNumber = "259")
@CardRegistration(set = "DMR", collectorNumber = "200")
public class SolkanarTheSwampKing extends Card {

    public SolkanarTheSwampKing() {
        addTrigger(Triggers.playerCastsSpell().withFilter(new CardColorPredicate(CardColor.BLACK)),
                new GainLifeEffect(1));
    }
}
