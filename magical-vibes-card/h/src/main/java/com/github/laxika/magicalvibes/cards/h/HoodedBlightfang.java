package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "104")
@CardRegistration(set = "M21", collectorNumber = "357")
public class HoodedBlightfang extends Card {

    public HoodedBlightfang() {
        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentHasKeywordPredicate(Keyword.DEATHTOUCH)),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentHasKeywordPredicate(Keyword.DEATHTOUCH)),
                new GainLifeEffect(1));
        addTrigger(Triggers.creatureYouControlDealsDamageToPlaneswalker(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasKeywordPredicate(Keyword.DEATHTOUCH),
                        new DestroyTargetPermanentEffect()));
    }
}
