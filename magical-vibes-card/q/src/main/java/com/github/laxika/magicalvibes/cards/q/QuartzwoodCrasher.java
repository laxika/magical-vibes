package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "201")
@CardRegistration(set = "LCC", collectorNumber = "281")
public class QuartzwoodCrasher extends Card {

    public QuartzwoodCrasher() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentHasKeywordPredicate(Keyword.TRAMPLE)),
                TriggerOptions.ONE_OR_MORE,
                new CreateTokenEffect(
                        "Dinosaur Beast", new EventValue(), new EventValue(), CardColor.GREEN,
                        List.of(CardSubtype.DINOSAUR, CardSubtype.BEAST), Set.of(Keyword.TRAMPLE), Set.of()));
    }
}
