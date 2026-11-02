package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "68")
public class MoonfolkPuzzlemaker extends Card {

    public MoonfolkPuzzlemaker() {
        addTrigger(Triggers.permanentYouControlBecomesTapped(), new TriggeringPermanentConditionalEffect(
                new PermanentIsSourceCardPredicate(), new ScryEffect(1)));
    }
}
