package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;

@CardRegistration(set = "CHK", collectorNumber = "215")
public class InameLifeAspect extends Card {

    public InameLifeAspect() {
        // "When Iname dies, you may exile it. If you do, return any number of target Spirit cards
        // from your graveyard to your hand." The Spirit targets are chosen as the trigger goes on
        // the stack; the exile is the resolution-time "you may" that gates the return.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                SequenceEffect.of(new ExileSourceCardFromGraveyardEffect(),
                        new ConditionalEffect(HappenedThisWay.exiled(), new ReturnTargetCardsFromGraveyardToHandEffect(
                                new CardSubtypePredicate(CardSubtype.SPIRIT), Integer.MAX_VALUE))),
                "Exile Iname, Life Aspect to return the targeted Spirit cards to your hand?"));
    }
}
