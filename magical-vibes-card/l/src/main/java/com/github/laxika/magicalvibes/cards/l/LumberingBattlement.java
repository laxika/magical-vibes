package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsExiledWithSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnNontokenCreaturesUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "15")
public class LumberingBattlement extends Card {

    public LumberingBattlement() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileOwnNontokenCreaturesUntilSourceLeavesEffect());
        addStaticEffect(new BoostSelfEffect(
                new Scaled(new CardsExiledWithSource(), 2),
                new Scaled(new CardsExiledWithSource(), 2)));
    }
}
