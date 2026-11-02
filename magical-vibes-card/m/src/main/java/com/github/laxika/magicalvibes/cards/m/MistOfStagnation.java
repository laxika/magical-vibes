package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsForAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JUD", collectorNumber = "48")
public class MistOfStagnation extends Card {

    public MistOfStagnation() {
        addStaticEffect(new MatchingPermanentsDoesntUntapEffect(new PermanentTruePredicate()));
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new UntapPermanentsForAmountEffect(
                        new CardsInGraveyard(null, CountScope.TARGET_PLAYER),
                        new PermanentTruePredicate()));
    }
}
