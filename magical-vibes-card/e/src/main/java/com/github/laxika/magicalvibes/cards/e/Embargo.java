package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "77")
public class Embargo extends Card {

    public Embargo() {
        addStaticEffect(new MatchingPermanentsDoesntUntapEffect(
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new LoseLifeEffect(2));
    }
}
