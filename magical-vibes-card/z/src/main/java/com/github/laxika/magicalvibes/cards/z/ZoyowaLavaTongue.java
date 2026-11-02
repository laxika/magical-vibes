package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.DescendedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMayDiscardOrSacrificePermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "245")
@CardRegistration(set = "LCI", collectorNumber = "312")
public class ZoyowaLavaTongue extends Card {

    public ZoyowaLavaTongue() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ConditionalEffect(new DescendedThisTurn(),
                        new EachOpponentMayDiscardOrSacrificePermanentEffect(3)));
    }
}
