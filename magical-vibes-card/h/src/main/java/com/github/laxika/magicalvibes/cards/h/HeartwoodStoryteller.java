package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.EachOpponentOfTriggeringPlayerMayDrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "127")
@CardRegistration(set = "TSR", collectorNumber = "209")
public class HeartwoodStoryteller extends Card {

    public HeartwoodStoryteller() {
        addTrigger(Triggers.playerCastsSpell()
                        .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new EachOpponentOfTriggeringPlayerMayDrawCardEffect());
    }
}
