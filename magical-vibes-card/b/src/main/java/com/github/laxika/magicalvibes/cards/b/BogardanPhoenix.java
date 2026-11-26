package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "76")
public class BogardanPhoenix extends Card {

    public BogardanPhoenix() {
        // Flying is auto-loaded from Scryfall keywords.
        // When this creature dies, exile it if it had a death counter on it. Otherwise, return it
        // to the battlefield under your control and put a death counter on it.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ConditionalReplacementEffect(
                new SourceCounterThreshold(1, CounterType.DEATH),
                new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(false, true, CounterType.DEATH, 1),
                ExileGraveyardCardsEffect.cards(Subjects.sourceCard())));
    }
}
