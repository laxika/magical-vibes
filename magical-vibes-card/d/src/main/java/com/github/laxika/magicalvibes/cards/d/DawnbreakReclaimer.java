package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DawnbreakReclaimerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1346")
@CardRegistration(set = "C15", collectorNumber = "2")
@CardRegistration(set = "CM2", collectorNumber = "24")
public class DawnbreakReclaimer extends Card {

    public DawnbreakReclaimer() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new DawnbreakReclaimerEffect(new CardTypePredicate(CardType.CREATURE)));
    }
}
