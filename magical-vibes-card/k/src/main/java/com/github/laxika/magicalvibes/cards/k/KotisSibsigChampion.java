package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CastSpellFromGraveyardOncePerYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFromGraveyardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "5")
public class KotisSibsigChampion extends Card {

    public KotisSibsigChampion() {
        addStaticEffect(new CastSpellFromGraveyardOncePerYourTurnEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(), 3));

        addTrigger(new TriggerCondition(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE,
                new EnteringCreatureFromGraveyardConditionalEffect(
                        new PutCountersOnSourceEffect(1, 1, 2)));
    }
}
