package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.TotalCountersOnSource;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECC", collectorNumber = "18")
@CardRegistration(set = "ECC", collectorNumber = "38")
public class PucasCovenant extends Card {

    public PucasCovenant() {
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), new TriggeringPermanentConditionalEffect(
                new PermanentHasCountersPredicate(CounterType.ANY),
                OncePerTurnTriggerEffect.markOnAcceptance(new MayEffect(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsPermanentPredicate())
                        .targetGraveyard(true)
                        .upTo(true)
                        .dynamicMaxManaValue(new TotalCountersOnSource())
                        .build(), "Return that card to your hand?"))));
    }
}
